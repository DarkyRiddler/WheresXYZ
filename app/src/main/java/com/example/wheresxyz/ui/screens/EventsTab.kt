package com.example.wheresxyz.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.example.wheresxyz.data.model.GroupItem
import com.example.wheresxyz.data.model.GroupMember
import com.example.wheresxyz.data.model.User
import com.example.wheresxyz.data.model.Event
import com.example.wheresxyz.ui.theme.*
import com.example.wheresxyz.ui.viewmodel.LocationSyncState
import com.example.wheresxyz.ui.viewmodel.LocationSyncViewModel
import com.example.wheresxyz.ui.viewmodel.EventsViewModel
import com.example.wheresxyz.ui.viewmodel.RemoteParticipant
import com.example.wheresxyz.util.GeoPoint
import com.example.wheresxyz.util.calculateOffsetLatLng
import com.example.wheresxyz.util.calculateDistanceMeters
import com.example.wheresxyz.util.formatDistanceMeters
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

data class RadarParticipant(
    val name: String,
    val avatar: String,
    val distanceMeters: Int,
    val angleDegrees: Double,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

private fun offsetFromBase(base: LatLng, distanceMeters: Int, angleDegrees: Double): LatLng {
    val offset = calculateOffsetLatLng(
        GeoPoint(base.latitude, base.longitude),
        distanceMeters.toDouble(),
        angleDegrees
    )
    return LatLng(offset.latitude, offset.longitude)
}

private fun formatEventDuration(start: Long, end: Long): String {
    val sdfDate = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
    val sdfTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    val startDateStr = sdfDate.format(java.util.Date(start))
    val endDateStr = sdfDate.format(java.util.Date(end))
    val startTimeStr = sdfTime.format(java.util.Date(start))
    val endTimeStr = sdfTime.format(java.util.Date(end))

    return if (startDateStr == endDateStr) {
        "$startDateStr $startTimeStr - $endTimeStr"
    } else {
        "$startDateStr $startTimeStr - $endDateStr $endTimeStr"
    }
}

private fun showDateTimePicker(context: Context, onDateTimeSelected: (Long) -> Unit) {
    val calendar = java.util.Calendar.getInstance()
    android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(java.util.Calendar.YEAR, year)
            calendar.set(java.util.Calendar.MONTH, month)
            calendar.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
            
            android.app.TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(java.util.Calendar.MINUTE, minute)
                    calendar.set(java.util.Calendar.SECOND, 0)
                    calendar.set(java.util.Calendar.MILLISECOND, 0)
                    onDateTimeSelected(calendar.timeInMillis)
                },
                calendar.get(java.util.Calendar.HOUR_OF_DAY),
                calendar.get(java.util.Calendar.MINUTE),
                true
            ).show()
        },
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH),
        calendar.get(java.util.Calendar.DAY_OF_MONTH)
    ).show()
}

private fun getInitials(name: String): String {
    val parts = name.trim().split("\\s+".toRegex())
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1).uppercase()
        else -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
    }
}

// Generate a beautiful circular marker bitmap with user's initials/avatar emoji inside
private fun createAvatarBitmapDescriptor(context: Context, text: String): BitmapDescriptor {
    val sizePx = 90
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = android.graphics.Color.parseColor("#4F46E5") // BrandIndigo (#4F46E5)
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, paint)
    
    paint.style = Paint.Style.STROKE
    paint.color = android.graphics.Color.WHITE
    paint.strokeWidth = 3f
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, (sizePx / 2f) - 1.5f, paint)
    
    paint.style = Paint.Style.FILL
    paint.color = android.graphics.Color.WHITE
    val isEmoji = text.any { it.code > 127 }
    paint.textSize = if (isEmoji) 36f else 28f
    paint.textAlign = Paint.Align.CENTER
    paint.isFakeBoldText = true
    
    val bounds = Rect()
    paint.getTextBounds(text, 0, text.length, bounds)
    val y = (sizePx / 2f) - bounds.exactCenterY()
    canvas.drawText(text, sizePx / 2f, y, paint)
    
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

@Composable
fun LiveLocationMapView(
    remoteParticipants: List<RemoteParticipant>,
    fallbackParticipants: List<RadarParticipant>,
    syncState: LocationSyncState,
    onLocationUpdate: (Double, Double) -> Unit,
    event: Event?,
    currentUserAvatar: String?,
    currentUserDisplayName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Default coordinates (Centrum Warszawy)
    var userLatLng by remember { mutableStateOf(LatLng(52.2297, 21.0122)) }

    DisposableEffect(hasPermission) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                userLatLng = LatLng(location.latitude, location.longitude)
                onLocationUpdate(location.latitude, location.longitude)
            }
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }
        
        if (hasPermission && locationManager != null) {
            try {
                val lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val lastNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val bestLocation = when {
                    lastGps != null && lastNet != null -> if (lastGps.time > lastNet.time) lastGps else lastNet
                    lastGps != null -> lastGps
                    else -> lastNet
                }
                bestLocation?.let {
                    userLatLng = LatLng(it.latitude, it.longitude)
                    onLocationUpdate(it.latitude, it.longitude)
                }

                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000L,
                    2f,
                    listener
                )
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    2000L,
                    2f,
                    listener
                )
            } catch (e: SecurityException) {
                // ignored
            }
        }
        
        onDispose {
            if (locationManager != null) {
                try {
                    locationManager.removeUpdates(listener)
                } catch (e: Exception) {
                    // ignored
                }
            }
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLatLng, 15f)
    }

    LaunchedEffect(userLatLng) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(userLatLng, 15f)
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            // User marker with their custom avatar emoji or initials
            val myAvatarText = currentUserAvatar?.takeIf { it.isNotEmpty() && it != "👤" } ?: getInitials(currentUserDisplayName)
            val myIconDescriptor = remember(myAvatarText) {
                createAvatarBitmapDescriptor(context, myAvatarText)
            }
            Marker(
                state = rememberMarkerState(position = userLatLng),
                title = "Ty (Twoja lokalizacja)",
                icon = myIconDescriptor
            )

            // Event start location marker & geofence circle
            if (event != null && event.startLatitude != 0.0 && event.startLongitude != 0.0) {
                Marker(
                    state = rememberMarkerState(position = LatLng(event.startLatitude, event.startLongitude)),
                    title = "Start: ${event.title}",
                    snippet = "Obszar: ${formatDistanceMeters(event.allowedDistance.toInt())}"
                )

                Circle(
                    center = LatLng(event.startLatitude, event.startLongitude),
                    radius = event.allowedDistance,
                    strokeColor = BrandIndigo,
                    fillColor = BrandIndigo.copy(alpha = 0.15f),
                    strokeWidth = 3f
                )
            }

            // Remote participants (Firebase sync) with custom avatar icons
            if (syncState == LocationSyncState.Active) {
                remoteParticipants.forEach { participant ->
                    val avatarText = participant.avatar.takeIf { it.isNotEmpty() && it != "👤" } ?: getInitials(participant.displayName)
                    val iconDescriptor = remember(avatarText) {
                        createAvatarBitmapDescriptor(context, avatarText)
                    }
                    Marker(
                        state = rememberMarkerState(position = LatLng(participant.latitude, participant.longitude)),
                        title = participant.displayName,
                        snippet = "Odległość: ${formatDistanceMeters(participant.distanceMeters)}",
                        icon = iconDescriptor
                    )
                }
            } else {
                fallbackParticipants.forEach { participant ->
                    val participantLatLng = LatLng(participant.latitude, participant.longitude)
                    val avatarText = participant.avatar.takeIf { it.isNotEmpty() && it != "👤" } ?: getInitials(participant.name)
                    val iconDescriptor = remember(avatarText) {
                        createAvatarBitmapDescriptor(context, avatarText)
                    }
                    Marker(
                        state = rememberMarkerState(position = participantLatLng),
                        title = participant.name,
                        snippet = "Odległość: ${formatDistanceMeters(participant.distanceMeters)}",
                        icon = iconDescriptor
                    )
                }
            }
        }

        if (!hasPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Wymagany dostęp do GPS",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aplikacja potrzebuje uprawnień do lokalizacji, aby wyświetlić Twoją pozycję w czasie rzeczywistym.",
                        color = TextSecondaryDark,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val permissions = mutableListOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            launcher.launch(permissions.toTypedArray())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo)
                    ) {
                        Text("Udziel uprawnień", color = Color.White)
                    }
                }
            }
        } else {
            val syncLabel = when (syncState) {
                LocationSyncState.Active -> "SYNCH. LOKALIZACJI AKTYWNA"
                LocationSyncState.Connecting -> "ŁĄCZENIE Z SYNCH..."
                LocationSyncState.Fallback -> "LOKALIZACJA GPS (TRYB DEMO)"
                LocationSyncState.Idle -> "LOKALIZACJA GPS AKTYWNA"
            }
            val syncColor = when (syncState) {
                LocationSyncState.Active -> SuccessGreen
                LocationSyncState.Connecting -> BrandCyan
                LocationSyncState.Fallback -> BrandRose
                LocationSyncState.Idle -> SuccessGreen
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(8.dp)
                    .background(syncColor.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                    .border(1.dp, syncColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = syncLabel,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LocationPickerMapView(
    selectedLatLng: LatLng,
    onLatLngSelected: (LatLng) -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLatLng, 14f)
    }

    // Centering the camera target on the selectedLatLng only once when it updates (like default to GPS position)
    var hasCenteredOnGps by remember { mutableStateOf(false) }
    LaunchedEffect(selectedLatLng) {
        if (!hasCenteredOnGps && selectedLatLng != LatLng(52.2297, 21.0122)) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(selectedLatLng, 14f)
            hasCenteredOnGps = true
        }
    }

    val markerState = rememberMarkerState()
    LaunchedEffect(selectedLatLng) {
        markerState.position = selectedLatLng
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                onLatLngSelected(latLng)
            }
        ) {
            Marker(
                state = markerState,
                title = "Punkt startowy"
            )
        }
    }
}

@Composable
fun EventsTab(
    groupsList: List<GroupItem>,
    currentUser: User,
    locationSyncViewModel: LocationSyncViewModel = hiltViewModel(),
    eventsViewModel: EventsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val syncState by locationSyncViewModel.syncState.collectAsState()
    val remoteParticipants by locationSyncViewModel.remoteParticipants.collectAsState()

    val eventsList by eventsViewModel.events.collectAsState()
    val isEventsLoading by eventsViewModel.isLoading.collectAsState()
    val eventsError by eventsViewModel.error.collectAsState()

    LaunchedEffect(groupsList) {
        eventsViewModel.loadEvents(groupsList)
    }

    var showCreateEventDialog by remember { mutableStateOf(false) }
    var selectedLiveEvent by remember { mutableStateOf<Event?>(null) }

    // GPS location permissions hook for adding event
    var hasCreateLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val createLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCreateLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(showCreateEventDialog) {
        if (showCreateEventDialog && !hasCreateLocationPermission) {
            createLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        EventsTabHeaderSection(
            syncState = syncState,
            isEventsLoading = isEventsLoading,
            isEventsEmpty = eventsList.isEmpty(),
            onStopSharing = { locationSyncViewModel.stopSharing() },
            onAddEventClick = { showCreateEventDialog = true }
        )

        if (!isEventsLoading && eventsList.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(eventsList) { event ->
                    val isActive = event.isActive
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .clickable { selectedLiveEvent = event },
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = event.title,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Z grupą: ${event.groupName}",
                                        fontSize = 14.sp,
                                        color = TextSecondaryDark
                                    )
                                    if (event.description.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = event.description,
                                            fontSize = 13.sp,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                if (isActive) {
                                    Box(
                                        modifier = Modifier
                                            .background(SuccessGreen.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                            .border(1.dp, SuccessGreen, RoundedCornerShape(10.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "NA ŻYWO",
                                            color = SuccessGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatEventDuration(event.startDate, event.endDate),
                                    fontSize = 12.sp,
                                    color = BrandCyan,
                                    fontWeight = FontWeight.Medium
                                )

                                if (isActive) {
                                    Box(
                                        modifier = Modifier
                                            .background(BrandRose.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                            .border(1.dp, BrandRose.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .clickable { selectedLiveEvent = event }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Notifications,
                                                contentDescription = "Pokaż mapę",
                                                tint = BrandRose,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Podejrzyj mapę",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BrandRose
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Nieaktywne",
                                        fontSize = 11.sp,
                                        color = TextSecondaryDark,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Live Event details with Map view
    if (selectedLiveEvent != null) {
        val event = selectedLiveEvent!!
        val isActive = event.isActive

        // Track user's latest GPS position
        var myLatestLatLng by remember { mutableStateOf(LatLng(52.2297, 21.0122)) }

        val activeParticipants = remember(event.id) {
            mutableStateListOf<RadarParticipant>().apply {
                val group = groupsList.firstOrNull { it.id == event.groupId }
                val members = group?.members?.filter { it.email != currentUser.email } ?: emptyList()
                if (members.isNotEmpty()) {
                    members.forEach { member ->
                        val distance = (100..1200).random()
                        val angle = (0..359).random().toDouble()
                        val pLatLng = offsetFromBase(myLatestLatLng, distance, angle)
                        add(
                            RadarParticipant(
                                name = "${member.name} ${member.lastname}".trim(),
                                avatar = member.avatar,
                                distanceMeters = distance,
                                angleDegrees = angle,
                                latitude = pLatLng.latitude,
                                longitude = pLatLng.longitude
                            )
                        )
                    }
                } else {
                    val p1 = offsetFromBase(myLatestLatLng, 150, 45.0)
                    val p2 = offsetFromBase(myLatestLatLng, 450, 120.0)
                    addAll(
                        listOf(
                            RadarParticipant("Kamil Nowak", "🧑‍💻", 150, 45.0, p1.latitude, p1.longitude),
                            RadarParticipant("Anna Zielińska", "🙋‍♀️", 450, 120.0, p2.latitude, p2.longitude)
                        )
                    )
                }
            }
        }

        if (isActive) {
            LaunchedEffect(event.id, currentUser.email) {
                locationSyncViewModel.startSharing(event.id, currentUser)
            }

            // We no longer call stopSharing() inside DisposableEffect's onDispose or dialog dismiss
            // to allow coordinates to continue streaming in the background as requested!
            LaunchedEffect(event.id, syncState) {
                if (syncState != LocationSyncState.Fallback) return@LaunchedEffect
                while (true) {
                    delay(3000L)
                    for (i in activeParticipants.indices) {
                        val participant = activeParticipants[i]
                        val deltaDistance = (-15..15).random()
                        val deltaAngle = (-8..8).random().toDouble()
                        val newDistance = (participant.distanceMeters + deltaDistance).coerceIn(10, 2000)
                        val newAngle = (participant.angleDegrees + deltaAngle + 360.0) % 360.0
                        val pLatLng = offsetFromBase(myLatestLatLng, newDistance, newAngle)
                        activeParticipants[i] = participant.copy(
                            distanceMeters = newDistance,
                            angleDegrees = newAngle,
                            latitude = pLatLng.latitude,
                            longitude = pLatLng.longitude
                        )
                    }
                }
            }
        }

        val displayParticipants = if (isActive) {
            if (syncState == LocationSyncState.Active) {
                remoteParticipants.map { remote ->
                    val dist = calculateDistanceMeters(
                        GeoPoint(myLatestLatLng.latitude, myLatestLatLng.longitude),
                        GeoPoint(remote.latitude, remote.longitude)
                    )
                    RadarParticipant(
                        name = remote.displayName,
                        avatar = remote.avatar,
                        distanceMeters = dist,
                        angleDegrees = 0.0,
                        latitude = remote.latitude,
                        longitude = remote.longitude
                    )
                }
            } else {
                activeParticipants.toList()
            }
        } else {
            emptyList()
        }

        // Calculate user's own distance to the event's start point
        val myDistToStart = calculateDistanceMeters(
            GeoPoint(myLatestLatLng.latitude, myLatestLatLng.longitude),
            GeoPoint(event.startLatitude, event.startLongitude)
        )
        val isIOutside = myDistToStart > event.allowedDistance

        var wasIOutside by remember { mutableStateOf(false) }
        LaunchedEffect(isIOutside) {
            if (isIOutside && !wasIOutside) {
                Toast.makeText(context, "⚠️ Opuściłeś obszar wydarzenia!", Toast.LENGTH_LONG).show()
            } else if (!isIOutside && wasIOutside) {
                Toast.makeText(context, "✅ Wróciłeś do obszaru wydarzenia.", Toast.LENGTH_LONG).show()
            }
            wasIOutside = isIOutside
        }

        // Track transitions of other participants (real or simulated) leaving/entering the allowed area
        val outsideUserKeys = remember { mutableStateMapOf<String, Boolean>() }
        LaunchedEffect(displayParticipants) {
            displayParticipants.forEach { participant ->
                val dist = calculateDistanceMeters(
                    GeoPoint(participant.latitude, participant.longitude),
                    GeoPoint(event.startLatitude, event.startLongitude)
                )
                val isOutside = dist > event.allowedDistance
                val wasOutside = outsideUserKeys[participant.name] == true
                if (isOutside && !wasOutside) {
                    outsideUserKeys[participant.name] = true
                    Toast.makeText(context, "⚠️ ${participant.name} opuścił obszar wydarzenia!", Toast.LENGTH_SHORT).show()
                } else if (!isOutside && wasOutside) {
                    outsideUserKeys[participant.name] = false
                    Toast.makeText(context, "✅ ${participant.name} wrócił do obszaru wydarzenia.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = {
                selectedLiveEvent = null
            }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = event.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Grupa: ${event.groupName}",
                        fontSize = 13.sp,
                        color = TextSecondaryDark
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isActive) {
                        // Live GPS Tracking Map (Fixed at the top, avoiding scroll overlap bugs!)
                        LiveLocationMapView(
                            remoteParticipants = remoteParticipants,
                            fallbackParticipants = activeParticipants.toList(),
                            syncState = syncState,
                            onLocationUpdate = { lat, lng ->
                                myLatestLatLng = LatLng(lat, lng)
                                locationSyncViewModel.updateMyLocation(lat, lng)
                            },
                            event = event,
                            currentUserAvatar = currentUser.userPhoto,
                            currentUserDisplayName = "${currentUser.name} ${currentUser.lastname}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Only the details and distances scroll below the map
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Czas trwania: ${formatEventDuration(event.startDate, event.endDate)}",
                            fontSize = 13.sp,
                            color = BrandCyan,
                            fontWeight = FontWeight.Medium
                        )

                        if (event.description.isNotEmpty()) {
                            Text(
                                text = event.description,
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        // Personal geofence warning banner
                        if (isIOutside && isActive) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.15f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, ErrorRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⚠️ Jesteś poza obszarem wydarzenia! (${formatDistanceMeters(myDistToStart)} / limit: ${formatDistanceMeters(event.allowedDistance.toInt())})",
                                        color = ErrorRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (isActive) {
                            Text(
                                text = "Odległości znajomych",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            if (displayParticipants.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Nikt jeszcze nie udostępnia lokalizacji.",
                                        color = TextSecondaryDark,
                                        fontSize = 12.sp
                                    )
                                }
                            } else {
                                // Render display participants directly inside scrollable column (removes LazyColumn nesting conflicts)
                                displayParticipants.forEach { participant ->
                                    val partDist = calculateDistanceMeters(
                                        GeoPoint(participant.latitude, participant.longitude),
                                        GeoPoint(event.startLatitude, event.startLongitude)
                                    )
                                    val isPartOutside = partDist > event.allowedDistance

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(12.dp))
                                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(
                                                        Brush.radialGradient(colors = listOf(BrandViolet, BrandIndigo)),
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val participantEmoji = participant.avatar.takeIf {
                                                    it.isNotEmpty() && it != "👤" && !it.startsWith("http") && !it.startsWith("content")
                                                }
                                                if (participantEmoji != null) {
                                                    Text(participantEmoji, fontSize = 14.sp)
                                                } else {
                                                    Text(
                                                        text = participant.name.take(1).uppercase().ifEmpty { "?" },
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = participant.name,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = Color.White
                                                    )
                                                    if (isPartOutside) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .background(ErrorRed.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                                .border(0.5.dp, ErrorRed, RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                                        ) {
                                                            Text("POZA OBSZAREM", color = ErrorRed, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = "Od Ciebie: ${formatDistanceMeters(participant.distanceMeters)} • Od startu: ${formatDistanceMeters(partDist)}",
                                                    fontSize = 11.sp,
                                                    color = BrandCyan
                                                )
                                            }
                                        }

                                        // Ping Button
                                        Box(
                                            modifier = Modifier
                                                .background(BrandRose.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                                .border(1.dp, BrandRose.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .clickable {
                                                    Toast.makeText(context, "Ping wysłany do: ${participant.name}!", Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Notifications,
                                                    contentDescription = "Ping",
                                                    tint = BrandRose,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = "Ping",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BrandRose
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Inactive event message
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "🔴 Wydarzenie nieaktywne",
                                        color = BrandRose,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "To wydarzenie odbędzie się w przyszłości lub już się zakończyło. Możliwość wspólnego podglądu pozycji na mapie jest aktywna tylko w czasie trwania wydarzenia.",
                                        color = TextSecondaryDark,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // If creator or group admin, allow deletion
                        val group = groupsList.firstOrNull { it.id == event.groupId }
                        val isCreator = event.createdBy == currentUser.email
                        val isAdmin = group?.isAdmin == true

                        if (isCreator || isAdmin) {
                            OutlinedButton(
                                onClick = {
                                    eventsViewModel.deleteEvent(event.id) {
                                        Toast.makeText(context, "Usunięto wydarzenie!", Toast.LENGTH_SHORT).show()
                                        selectedLiveEvent = null
                                    }
                                },
                                border = BorderStroke(1.dp, ErrorRed),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Usuń", color = ErrorRed, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                selectedLiveEvent = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(if (isCreator || isAdmin) 1f else 2f)
                        ) {
                            Text("Zamknij", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Add Event Dialog (Optimized layout structure: Map is fixed at the top, form scrolls below to completely avoid scroll container conflict issues)
    if (showCreateEventDialog) {
        var eventTitleInput by remember { mutableStateOf("") }
        var eventDescriptionInput by remember { mutableStateOf("") }
        var selectedGroupIndex by remember { mutableStateOf(0) }
        var startDateVal by remember { mutableStateOf(0L) }
        var endDateVal by remember { mutableStateOf(0L) }
        var pickedLocation by remember { mutableStateOf(LatLng(52.2297, 21.0122)) } // Default to Warsaw
        var allowedDistanceInput by remember { mutableStateOf("500") } // Default to 500 meters
        
        val eligibleGroups = groupsList.filter { 
            it.isAdmin || (it.members.find { m -> m.isMe }?.canCreateEvents == true) 
        }

        // Fetch user's current GPS location to center the picker coordinates automatically when dialog opens
        LaunchedEffect(hasCreateLocationPermission) {
            if (hasCreateLocationPermission) {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                if (locationManager != null) {
                    try {
                        val lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        val lastNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        val bestLocation = when {
                            lastGps != null && lastNet != null -> if (lastGps.time > lastNet.time) lastGps else lastNet
                            lastGps != null -> lastGps
                            else -> lastNet
                        }
                        bestLocation?.let {
                            pickedLocation = LatLng(it.latitude, it.longitude)
                        }
                    } catch (e: SecurityException) {
                        // ignore
                    }
                }
            }
        }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showCreateEventDialog = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Dodaj Wydarzenie",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. Fixed Map Picker at the top of the dialog card - completely avoids scroll container conflict issues
                    Text("Miejsce startu (kliknij na mapie):", color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    LocationPickerMapView(
                        selectedLatLng = pickedLocation,
                        onLatLngSelected = { pickedLocation = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Scrollable container for input fields below the map picker
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (eligibleGroups.isEmpty()) {
                            Text(
                                text = "Nie należysz do żadnej grupy w której masz uprawnienia do tworzenia wydarzeń. Musisz być administratorem grupy lub otrzymać odpowiednie uprawnienie.",
                                color = ErrorRed,
                                fontSize = 14.sp
                            )
                        } else {
                            // Title
                            OutlinedTextField(
                                value = eventTitleInput,
                                onValueChange = { eventTitleInput = it },
                                label = { Text("Nazwa wydarzenia", color = TextSecondaryDark) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandIndigo,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Description
                            OutlinedTextField(
                                value = eventDescriptionInput,
                                onValueChange = { eventDescriptionInput = it },
                                label = { Text("Opis wydarzenia", color = TextSecondaryDark) },
                                maxLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandIndigo,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Group Selector
                            Text("Wybierz grupę:", color = Color.White, fontSize = 14.sp)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                eligibleGroups.forEachIndexed { idx, group ->
                                    val isSelected = selectedGroupIndex == idx
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isSelected) BrandIndigo.copy(alpha = 0.2f) else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) BrandIndigo else Color.White.copy(alpha = 0.15f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedGroupIndex = idx }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = group.name,
                                            fontSize = 12.sp,
                                            color = if (isSelected) BrandCyan else TextSecondaryDark
                                        )
                                    }
                                }
                            }

                            // Allowed Distance
                            OutlinedTextField(
                                value = allowedDistanceInput,
                                onValueChange = { allowedDistanceInput = it.filter { char -> char.isDigit() } },
                                label = { Text("Dozwolony promień (w metrach)", color = TextSecondaryDark) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandIndigo,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Start Date Button
                            Text("Rozpoczęcie:", color = TextSecondaryDark, fontSize = 12.sp)
                            Button(
                                onClick = {
                                    showDateTimePicker(context) { timestamp ->
                                        startDateVal = timestamp
                                        if (endDateVal <= timestamp) {
                                            endDateVal = timestamp + 2 * 60 * 60 * 1000
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val formatEventDateTime = { timestamp: Long ->
                                        if (timestamp == 0L) "Wybierz datę i godzinę..."
                                        else {
                                            val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                                            sdf.format(java.util.Date(timestamp))
                                        }
                                    }
                                    Text(
                                        text = formatEventDateTime(startDateVal),
                                        color = if (startDateVal == 0L) TextSecondaryDark else Color.White,
                                        fontSize = 14.sp
                                    )
                                    Icon(Icons.Default.Today, contentDescription = "Wybierz datę", tint = BrandCyan)
                                }
                            }

                            // End Date Button
                            Text("Zakończenie:", color = TextSecondaryDark, fontSize = 12.sp)
                            Button(
                                onClick = {
                                    showDateTimePicker(context) { timestamp ->
                                        endDateVal = timestamp
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val formatEventDateTime = { timestamp: Long ->
                                        if (timestamp == 0L) "Wybierz datę i godzinę..."
                                        else {
                                            val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                                            sdf.format(java.util.Date(timestamp))
                                        }
                                    }
                                    Text(
                                        text = formatEventDateTime(endDateVal),
                                        color = if (endDateVal == 0L) TextSecondaryDark else Color.White,
                                        fontSize = 14.sp
                                    )
                                    Icon(Icons.Default.Today, contentDescription = "Wybierz datę", tint = BrandCyan)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showCreateEventDialog = false },
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Anuluj", color = Color.White)
                        }

                        if (eligibleGroups.isNotEmpty()) {
                            val distVal = allowedDistanceInput.toDoubleOrNull() ?: 0.0
                            val isConfirmEnabled = eventTitleInput.isNotBlank() && startDateVal > 0 && endDateVal > startDateVal && distVal > 0.0
                            
                            Button(
                                onClick = {
                                    if (isConfirmEnabled) {
                                        val selectedGroup = eligibleGroups[selectedGroupIndex]
                                        eventsViewModel.createEvent(
                                            title = eventTitleInput,
                                            description = eventDescriptionInput,
                                            startDate = startDateVal,
                                            endDate = endDateVal,
                                            groupId = selectedGroup.id,
                                            groupName = selectedGroup.name,
                                            createdBy = currentUser.email,
                                            startLatitude = pickedLocation.latitude,
                                            startLongitude = pickedLocation.longitude,
                                            allowedDistance = distVal
                                        ) {
                                            Toast.makeText(context, "Utworzono wydarzenie!", Toast.LENGTH_SHORT).show()
                                            showCreateEventDialog = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                                enabled = isConfirmEnabled,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Utwórz", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventsTabHeaderSection(
    syncState: LocationSyncState,
    isEventsLoading: Boolean,
    isEventsEmpty: Boolean,
    onStopSharing: () -> Unit,
    onAddEventClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (syncState == LocationSyncState.Active) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(1.dp, SuccessGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(SuccessGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Udostępniasz lokalizację w tle",
                            color = SuccessGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Zatrzymaj",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .clickable(onClick = onStopSharing)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Wydarzenia",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Button(
                onClick = onAddEventClick,
                colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Dodaj Wydarzenie", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isEventsLoading && isEventsEmpty) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandIndigo)
            }
        } else if (isEventsEmpty) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = "Brak nadchodzących wydarzeń.\nStwórz nowe, klikając przycisk u góry!",
                    color = TextSecondaryDark,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
