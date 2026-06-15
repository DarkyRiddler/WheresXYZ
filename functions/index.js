const functions = require('firebase-functions/v1');
const admin = require('firebase-admin');

admin.initializeApp();

const db = admin.firestore();
const rtdb = admin.database();

/**
 * Calculates distance (in meters) between two coordinates using the Haversine formula.
 */
function calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371000; // Earth's radius in meters
    const phi1 = (lat1 * Math.PI) / 180;
    const phi2 = (lat2 * Math.PI) / 180;
    const deltaPhi = ((lat2 - lat1) * Math.PI) / 180;
    const deltaLambda = ((lon2 - lon1) * Math.PI) / 180;

    const a =
        Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
        Math.cos(phi1) * Math.cos(phi2) *
        Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c;
}

/**
 * Helper to fetch FCM tokens for all members of a group except a specific email.
 */
async function getGroupMemberTokens(groupId, excludeEmail = null) {
    try {
        const groupDoc = await db.collection('Groups').doc(groupId).get();
        if (!groupDoc.exists) return [];

        const members = groupDoc.data().members || [];
        const tokens = [];

        for (const member of members) {
            const email = member.email;
            if (excludeEmail && email.toLowerCase() === excludeEmail.toLowerCase()) {
                continue;
            }

            // Find user in Firestore by email
            const userSnapshot = await db.collection('Users')
                .where('email', '==', email)
                .limit(1)
                .get();

            if (!userSnapshot.empty) {
                const userData = userSnapshot.docs[0].data();
                if (userData.fcm_token) {
                    tokens.push(userData.fcm_token);
                }
            }
        }
        return tokens;
    } catch (e) {
        console.error('Error fetching group tokens:', e);
        return [];
    }
}

/**
 * Scenario 1: Triggered when an event is created in Firestore.
 */
exports.onEventCreated = functions.region('europe-west1').firestore
    .document('Events/{eventId}')
    .onCreate(async (snapshot, context) => {
        const event = snapshot.data();
        const createdBy = event.createdBy;

        const tokens = await getGroupMemberTokens(event.groupId, createdBy);
        if (tokens.length === 0) return null;

        const message = {
            notification: {
                title: `Nowe wydarzenie: ${event.groupName}`,
                body: `Utworzono "${event.title}". Rozpocznie się: ${new Date(event.startDate).toLocaleString('pl-PL')}`
            },
            tokens: tokens
        };

        const response = await admin.messaging().sendEachForMulticast(message);
        console.log(`Successfully sent event creation notice. Success count: ${response.successCount}`);
        return null;
    });

/**
 * Scenario 2: Runs every minute to check and notify when events start.
 */
exports.checkEventStarts = functions.region('europe-west1').pubsub
    .schedule('* * * * *')
    .onRun(async (context) => {
        const now = Date.now();
        // Query active events that haven't sent a start notification yet
        const querySnapshot = await db.collection('Events')
            .where('startDate', '<=', now)
            .get();

        for (const doc of querySnapshot.docs) {
            const event = doc.data();
            // Check if end date is in the future and notification hasn't been sent
            if (event.endDate > now && !event.startedNotificationSent) {
                const tokens = await getGroupMemberTokens(event.groupId);
                if (tokens.length > 0) {
                    const message = {
                        notification: {
                            title: `Wydarzenie wystartowało! 🚀`,
                            body: `Rozpoczęło się wydarzenie "${event.title}". Udostępnianie lokalizacji jest teraz aktywne.`
                        },
                        tokens: tokens
                    };
                    await admin.messaging().sendEachForMulticast(message);
                }

                // Mark as notified in Firestore
                await doc.ref.update({ startedNotificationSent: true });
                console.log(`Sent start notification for event: ${event.title}`);
            }
        }
        return null;
    });

/**
 * Scenario 3 & 4: Triggered when coordinates are updated in Realtime Database.
 * Calculates geofencing breaches and notifies members accordingly.
 */
exports.onLiveLocationUpdated = functions.region('europe-west1').database
    .ref('/live_locations/{eventId}/{userKey}')
    .onWrite(async (change, context) => {
        const eventId = context.params.eventId;
        const userKey = context.params.userKey;

        // Exit if data is deleted
        if (!change.after.exists()) return null;

        const locationData = change.after.val();
        const userLat = locationData.latitude;
        const userLng = locationData.longitude;
        const displayName = locationData.displayName;

        // Fetch Event rules from Firestore
        const eventDoc = await db.collection('Events').doc(eventId).get();
        if (!eventDoc.exists) return null;

        const event = eventDoc.data();
        const startLat = event.startLatitude;
        const startLng = event.startLongitude;
        const allowedDistance = event.allowedDistance;

        // Calculate if user is currently outside the boundary
        const distance = calculateDistance(userLat, userLng, startLat, startLng);
        const isOutsideNow = distance > allowedDistance;

        console.log(`[Geofence] User ${displayName} (${userKey}) is currently ${distance.toFixed(1)}m away from start. Allowed: ${allowedDistance}m. isOutsideNow: ${isOutsideNow}`);

        const geofenceStateRef = rtdb.ref(`/geofence_states/${eventId}/${userKey}`);
        
        // Read current state first to populate cache and log status
        const stateSnapshot = await geofenceStateRef.get();
        const exists = stateSnapshot.exists();
        const wasOutside = exists ? stateSnapshot.val() : null;

        console.log(`[Geofence] Previous state for user ${userKey} exists: ${exists}, value: ${wasOutside}`);

        let resultCommitted = false;
        let finalState = wasOutside;

        // If the state doesn't exist yet:
        if (!exists) {
            if (isOutsideNow) {
                // Transition to true (outside)
                const txResult = await geofenceStateRef.transaction((currentValue) => {
                    // Force server sync if local is null
                    if (currentValue === null) return true;
                    if (currentValue !== true) return true;
                    return; // Abort if already true
                });
                resultCommitted = txResult.committed;
                finalState = true;
            } else {
                // Initialize to false (inside) without notifying
                await geofenceStateRef.set(false);
                resultCommitted = false;
                finalState = false;
                console.log(`[Geofence] Initialized state to false for user ${userKey}`);
            }
        } else {
            // State exists, check if it changed
            if (isOutsideNow !== wasOutside) {
                const txResult = await geofenceStateRef.transaction((currentValue) => {
                    if (currentValue === null) return isOutsideNow;
                    if (currentValue !== isOutsideNow) return isOutsideNow;
                    return; // Abort
                });
                resultCommitted = txResult.committed;
                finalState = isOutsideNow;
            } else {
                console.log(`[Geofence] State has not changed (${wasOutside} -> ${isOutsideNow}). Skipping transaction.`);
            }
        }

        console.log(`[Geofence] Transaction committed: ${resultCommitted}, finalState: ${finalState}`);

        if (resultCommitted) {
            // Find the user's registered email to exclude them from group notifications
            const userSnapshot = await db.collection('Users')
                .where('email', '==', locationData.userKey)
                .limit(1)
                .get();

            const userEmail = !userSnapshot.empty ? userSnapshot.docs[0].data().email : null;
            const groupTokens = await getGroupMemberTokens(event.groupId, userEmail);

            if (finalState === true) {
                // Scenario 3: User left the area
                // 1. Notify the user who left
                if (!userSnapshot.empty && userSnapshot.docs[0].data().fcm_token) {
                    await admin.messaging().send({
                        token: userSnapshot.docs[0].data().fcm_token,
                        notification: {
                            title: `⚠️ Opuściłeś obszar!`,
                            body: `Wyszedłeś poza dozwolony obszar wydarzenia "${event.title}".`
                        }
                    });
                }

                // 2. Notify other group members
                if (groupTokens.length > 0) {
                    await admin.messaging().sendEachForMulticast({
                        tokens: groupTokens,
                        notification: {
                            title: `⚠️ Użytkownik poza obszarem`,
                            body: `${displayName} opuścił obszar wydarzenia "${event.title}".`
                        }
                    });
                }
                console.log(`[Geofence Notification] Sent LEFT notice for ${displayName}`);
            } else if (finalState === false) {
                // Scenario 4: User rejoined the area
                // Notify other group members
                if (groupTokens.length > 0) {
                    await admin.messaging().sendEachForMulticast({
                        tokens: groupTokens,
                        notification: {
                            title: `✅ Użytkownik wrócił do obszaru`,
                            body: `${displayName} wrócił do obszaru wydarzenia "${event.title}".`
                        }
                    });
                }
                console.log(`[Geofence Notification] Sent REJOINED notice for ${displayName}`);
            }
        }

        return null;
    });

/**
 * Scenario 5: Triggered when a ping is created in the Realtime Database.
 * Sends a push notification to the target user, then deletes the ping request.
 */
exports.onPingCreated = functions.region('europe-west1').database
    .ref('/pings/{pingId}')
    .onCreate(async (snapshot, context) => {
        const ping = snapshot.val();
        if (!ping) return null;

        const senderName = ping.senderName;
        const senderEmail = ping.senderEmail;
        const targetEmail = ping.targetEmail;
        const targetGroupId = ping.targetGroupId;

        if (targetGroupId) {
            // Group ping: Send to all group members except the sender
            const tokens = await getGroupMemberTokens(targetGroupId, senderEmail);
            if (tokens.length > 0) {
                const message = {
                    tokens: tokens,
                    notification: {
                        title: `Ping grupy! 🔔`,
                        body: `${senderName} spingował grupę!`
                    }
                };
                await admin.messaging().sendEachForMulticast(message);
                console.log(`Sent group ping from ${senderName} to group ${targetGroupId}`);
            } else {
                console.log(`No tokens found for group ${targetGroupId}`);
            }
        } else if (targetEmail) {
            // Personal ping: Find the target user in Firestore by email
            const userSnapshot = await db.collection('Users')
                .where('email', '==', targetEmail)
                .limit(1)
                .get();

            if (!userSnapshot.empty) {
                const userData = userSnapshot.docs[0].data();
                if (userData.fcm_token) {
                    const message = {
                        token: userData.fcm_token,
                        notification: {
                            title: `Zostałeś spingowany! 🔔`,
                            body: `${senderName} Cię spingował!`
                        }
                    };

                    await admin.messaging().send(message);
                    console.log(`Sent personal ping from ${senderName} to ${targetEmail}`);
                } else {
                    console.log(`No FCM token registered for user ${targetEmail}`);
                }
            } else {
                console.log(`No user found with email ${targetEmail}`);
            }
        }

        // Clean up the temporary ping node
        await snapshot.ref.remove();
        return null;
    });
