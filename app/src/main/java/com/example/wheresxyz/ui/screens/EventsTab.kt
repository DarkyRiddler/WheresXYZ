package com.example.wheresxyz.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.example.wheresxyz.data.model.User
import com.example.wheresxyz.ui.theme.*
import com.example.wheresxyz.ui.viewmodel.LocationSyncState
import com.example.wheresxyz.ui.viewmodel.LocationSyncViewModel
import com.example.wheresxyz.ui.viewmodel.RemoteParticipant
import com.example.wheresxyz.util.GeoPoint
import com.example.wheresxyz.util.calculateOffsetLatLng
import com.example.wheresxyz.util.formatDistanceMeters
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

data class RadarParticipant(
    val name: String,
    val avatar: String,
    val distanceMeters: Int,
    val angleDegrees: Double
)

data class EventItem(
    val id: String,
    val title: String,
    val groupName: String,
    val groupId: String,
    val date: String,
    val isActive: Boolean,
    val participants: List<RadarParticipant>
)

// Math helper kept for fallback mock mode — see util/LocationMath.kt for shared implementation
private fun offsetFromBase(base: LatLng, distanceMeters: Int, angleDegrees: Double): LatLng {
    val offset = calculateOffsetLatLng(
        GeoPoint(base.latitude, base.longitude),
        distanceMeters.toDouble(),
        angleDegrees
    )
    return LatLng(offset.latitude, offset.longitude)
}

@Composable
fun LiveLocationMapView(
    remoteParticipants: List<RemoteParticipant>,
    fallbackParticipants: List<RadarParticipant>,
    syncState: LocationSyncState,
    onLocationUpdate: (Double, Double) -> Unit,
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
            // User marker
            Marker(
                state = rememberMarkerState(position = userLatLng),
                title = "Ty (Twoja lokalizacja)"
            )

            // Remote participants (Firebase sync) or fallback mock offsets
            if (syncState == LocationSyncState.Active) {
                remoteParticipants.forEach { participant ->
                    Marker(
                        state = rememberMarkerState(position = LatLng(participant.latitude, participant.longitude)),
                        title = participant.displayName,
                        snippet = "Odległość: ${formatDistanceMeters(participant.distanceMeters)}"
                    )
                }
            } else {
                fallbackParticipants.forEach { participant ->
                    val participantLatLng = offsetFromBase(
                        userLatLng,
                        participant.distanceMeters,
                        participant.angleDegrees
                    )
                    Marker(
                        state = rememberMarkerState(position = participantLatLng),
                        title = participant.name,
                        snippet = "Odległość: ${formatDistanceMeters(participant.distanceMeters)}"
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
                            launcher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
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
fun EventsTab(
    groupsList: List<GroupItem>,
    currentUser: User,
    locationSyncViewModel: LocationSyncViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val syncState by locationSyncViewModel.syncState.collectAsState()
    val remoteParticipants by locationSyncViewModel.remoteParticipants.collectAsState()

    val eventsList = remember {
        mutableStateListOf(
            EventItem(
                id = "1",
                title = "Festiwal Letnich Rytmów",
                groupName = "Ekipa Festiwalowa",
                groupId = "1",
                date = "Dziś o 20:00",
                isActive = true,
                participants = listOf(
                    RadarParticipant("Kamil Nowak", "🧑‍💻", 150, 45.0),
                    RadarParticipant("Anna Zielińska", "🙋‍♀️", 450, 120.0),
                    RadarParticipant("Piotr Wiśniewski", "🧑‍🚀", 1100, 270.0),
                    RadarParticipant("Maja Szymańska", "🧑‍🎨", 850, 330.0)
                )
            ),
            EventItem(
                id = "2",
                title = "Weekendowy Szlak Górski",
                groupName = "Grupa Górska",
                groupId = "3",
                date = "Niedziela o 08:00",
                isActive = false,
                participants = listOf(
                    RadarParticipant("Tomek Wójcik", "🧑‍🚀", 220, 90.0),
                    RadarParticipant("Ola Kowalczyk", "🧑‍🎨", 710, 210.0)
                )
            )
        )
    }

    var showCreateEventDialog by remember { mutableStateOf(false) }
    var selectedLiveEvent by remember { mutableStateOf<EventItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
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
                onClick = { showCreateEventDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Dodaj Wydarzenie", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(eventsList) { event ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .clickable { if (event.isActive) selectedLiveEvent = event },
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
                            }

                            if (event.isActive) {
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
                                text = event.date,
                                fontSize = 12.sp,
                                color = BrandCyan,
                                fontWeight = FontWeight.Medium
                            )

                            if (event.isActive) {
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

        val activeParticipants = remember(event.id) {
            mutableStateListOf<RadarParticipant>().apply { addAll(event.participants) }
        }

        LaunchedEffect(event.id, currentUser.email) {
            locationSyncViewModel.startSharing(event.id, currentUser)
        }

        DisposableEffect(event.id) {
            onDispose {
                locationSyncViewModel.stopSharing()
            }
        }

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
                    activeParticipants[i] = participant.copy(
                        distanceMeters = newDistance,
                        angleDegrees = newAngle
                    )
                }
            }
        }

        val displayParticipants = if (syncState == LocationSyncState.Active) {
            remoteParticipants.map { remote ->
                RadarParticipant(
                    name = remote.displayName,
                    avatar = remote.avatar,
                    distanceMeters = remote.distanceMeters,
                    angleDegrees = 0.0
                )
            }
        } else {
            activeParticipants.toList()
        }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = {
                locationSyncViewModel.stopSharing()
                selectedLiveEvent = null
            }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 620.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
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
                        }
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live GPS Tracking Map
                    LiveLocationMapView(
                        remoteParticipants = remoteParticipants,
                        fallbackParticipants = activeParticipants.toList(),
                        syncState = syncState,
                        onLocationUpdate = { lat, lng ->
                            locationSyncViewModel.updateMyLocation(lat, lng)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Odległości znajomych",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayParticipants) { participant ->
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
                                        if (participant.avatar.startsWith("content://") || participant.avatar.startsWith("file://")) {
                                            val bitmap = rememberUriImage(participant.avatar, context)
                                            if (bitmap != null) {
                                                Image(
                                                    bitmap = bitmap,
                                                    contentDescription = participant.name,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                                )
                                            } else {
                                                Text(participant.name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Text(participant.avatar, fontSize = 14.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = participant.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Dystans: ${formatDistanceMeters(participant.distanceMeters)}",
                                            fontSize = 12.sp,
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            locationSyncViewModel.stopSharing()
                            selectedLiveEvent = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Zamknij", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Add Event Dialog
    if (showCreateEventDialog) {
        var eventTitleInput by remember { mutableStateOf("") }
        var selectedGroupIndex by remember { mutableStateOf(0) }
        var isLiveInput by remember { mutableStateOf(true) }
        var eventDateInput by remember { mutableStateOf("") }
        
        val eligibleGroups = groupsList.filter { 
            it.isAdmin || (it.members.find { m -> m.isMe }?.canCreateEvents == true) 
        }

        AlertDialog(
            onDismissRequest = { showCreateEventDialog = false },
            title = { Text("Dodaj Wydarzenie", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
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

                        Spacer(modifier = Modifier.height(12.dp))

                        // Group Selector
                        Text("Wybierz grupę:", color = Color.White, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Is Live Switch/Checkbox
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isLiveInput,
                                onCheckedChange = { isLiveInput = it },
                                colors = CheckboxDefaults.colors(checkedColor = BrandIndigo)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rozpocznij teraz (Wydarzenie na żywo)", color = Color.White, fontSize = 14.sp)
                        }

                        if (!isLiveInput) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = eventDateInput,
                                onValueChange = { eventDateInput = it },
                                label = { Text("Kiedy się odbędzie (np. Jutro o 18:00)", color = TextSecondaryDark) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandIndigo,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (eligibleGroups.isNotEmpty()) {
                    Button(
                        onClick = {
                            if (eventTitleInput.isNotBlank()) {
                                val selectedGroup = eligibleGroups[selectedGroupIndex]
                                val groupMembers = selectedGroup.members.filter { !it.isMe }
                                val participants = groupMembers.map { member ->
                                    RadarParticipant(
                                        name = "${member.name} ${member.lastname}",
                                        avatar = member.avatar,
                                        distanceMeters = (50..1200).random(),
                                        angleDegrees = (0..359).random().toDouble()
                                    )
                                }

                                eventsList.add(
                                    EventItem(
                                        id = (100..999).random().toString(),
                                        title = eventTitleInput,
                                        groupName = selectedGroup.name,
                                        groupId = selectedGroup.id,
                                        date = if (isLiveInput) "Rozpoczęło się teraz" else eventDateInput,
                                        isActive = isLiveInput,
                                        participants = participants
                                    )
                                )
                                showCreateEventDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                        enabled = eventTitleInput.isNotBlank() && (isLiveInput || eventDateInput.isNotBlank())
                    ) {
                        Text("Utwórz", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateEventDialog = false }) {
                    Text("Anuluj", color = Color.White)
                }
            },
            containerColor = DarkSurface
        )
    }
}
