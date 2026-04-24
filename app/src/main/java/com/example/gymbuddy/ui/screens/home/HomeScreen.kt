package com.example.gymbuddy.ui.screens.home

import android.Manifest
import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymbuddy.domain.model.GymChain
import com.example.gymbuddy.domain.model.GymLocation
import com.example.gymbuddy.domain.model.PetMood
import com.example.gymbuddy.domain.model.UserTitle
import com.example.gymbuddy.ui.navigation.Screen
import com.example.gymbuddy.ui.theme.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.util.Calendar
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val petChatViewModel: PetChatViewModel = hiltViewModel()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            viewModel.onLocationPermissionGranted()
        }
    }

    LaunchedEffect(Unit) {
        if (!viewModel.hasLocationPermission()) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    var showPetChat by remember { mutableStateOf(false) }
    var petInteractionIndex by remember { mutableIntStateOf(0) }
    var lastPetInteractionTime by remember { mutableLongStateOf(0L) }

    val handlePetInteraction: () -> Unit = {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastPetInteractionTime > 1000) {
            val mood = uiState.effectivePetMood
            if (mood.extraMessages.isNotEmpty()) {
                petInteractionIndex = (petInteractionIndex + 1) % (mood.extraMessages.size + 1)
            }
            lastPetInteractionTime = currentTime
        }
    }

    val petChatState by petChatViewModel.uiState.collectAsState()

    if (showPetChat) {
        PetChatDialog(
            messages = petChatState.messages,
            isLoading = petChatState.isLoading,
            petEmoji = petChatState.petEmoji,
            aiPetEmoji = petChatState.aiPetEmoji,
            petName = petChatState.petName,
            userAvatarUri = petChatState.userAvatarUri,
            onSendMessage = { message -> petChatViewModel.sendMessage(message) },
            onDismiss = {
                showPetChat = false
                petChatViewModel.onGenerationComplete()
            }
        )

        LaunchedEffect(Unit) {
            petChatViewModel.initializeAndGenerateGreeting()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        GreetingSection(
            greeting = getTimeBasedGreeting(),
            userName = uiState.userProfile?.name ?: "Trainer",
            avatarUri = uiState.userProfile?.avatarUri,
            level = uiState.userProfile?.level ?: 1,
            xp = uiState.userProfile?.xp ?: 0,
            title = uiState.userProfile?.title ?: "Novice"
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        PetSection(
            petName = uiState.userProfile?.pet?.name ?: "GymBot",
            happiness = uiState.effectivePetHappiness,
            mood = uiState.effectivePetMood,
            interactionIndex = petInteractionIndex,
            onInteraction = { showPetChat = true }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        StatsSection(
            currentStreak = uiState.userProfile?.currentStreak ?: 0,
            longestStreak = uiState.userProfile?.longestStreak ?: 0,
            totalWorkouts = uiState.userProfile?.totalWorkouts ?: 0
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        GymFinderSection(
            gymChains = viewModel.gymChains,
            selectedChain = uiState.selectedGymChain,
            nearestGym = uiState.nearestGym,
            chainGymLocations = uiState.chainGymLocations,
            userLocation = uiState.userLocation,
            isLoading = uiState.isLoadingLocation,
            error = uiState.locationError,
            onGymSelected = { viewModel.selectGymChain(it) },
            onRequestPermission = {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        val nextRoutine = uiState.nextWorkout
        if (nextRoutine != null) {
            NextWorkoutCard(
                routineName = nextRoutine.name,
                routineType = nextRoutine.type,
                exerciseCount = nextRoutine.exercises.size,
                estimatedMinutes = nextRoutine.estimatedMinutes,
                onStart = { navController.navigate(Screen.ActiveWorkout.route) }
            )
        }
    }
}

@Composable
private fun getTimeBasedGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..20 -> "Good evening"
        else -> "Good night"
    }
}

@Composable
fun GreetingSection(greeting: String, userName: String, avatarUri: String?, level: Int, xp: Int, title: String) {
    Column {
        Text(
            text = greeting,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (avatarUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatarUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        if (title.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = XpGold.copy(alpha = 0.2f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = XpGold,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        XpProgressBar(level = level, xp = xp)
    }
}

@Composable
fun XpProgressBar(level: Int, xp: Int) {
    val xpForNextLevel = level * 1000
    val progress = (xp.toFloat() / xpForNextLevel).coerceIn(0f, 1f)
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Level $level",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = NeonTeal
            )
            Text(
                text = "$xp / $xpForNextLevel XP",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(ProgressBarBackground)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(NeonTeal, NeonCyan)
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@Composable
fun PetSection(
    petName: String,
    happiness: Int,
    mood: PetMood,
    interactionIndex: Int = 0,
    onInteraction: () -> Unit = {}
) {
    val animatedScale = remember { androidx.compose.runtime.mutableFloatStateOf(1f) }

    val displayedMessage = if (interactionIndex > 0 && mood.extraMessages.isNotEmpty()) {
        mood.extraMessages.getOrNull((interactionIndex - 1) % mood.extraMessages.size) ?: mood.message
    } else {
        mood.message
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInteraction() },
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                when {
                                    happiness > 70 -> PetHappy
                                    happiness > 40 -> PetExcited
                                    else -> PetSad
                                },
                                DarkSurfaceElevated
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mood.emoji,
                    fontSize = 32.sp,
                    modifier = Modifier.graphicsLayer {
                        scaleX = animatedScale.value
                        scaleY = animatedScale.value
                    }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = petName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = mood.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "\"$displayedMessage\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (interactionIndex > 0) NeonTeal else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            CircularProgressIndicator(
                progress = { happiness / 100f },
                modifier = Modifier.size(48.dp),
                color = when {
                    happiness > 70 -> SuccessGreen
                    happiness > 40 -> WarningOrange
                    else -> ErrorRed
                },
                trackColor = ProgressBarBackground,
                strokeWidth = 4.dp
            )
        }
    }
}

@Composable
fun StatsSection(currentStreak: Int, longestStreak: Int, totalWorkouts: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Streak",
            value = "$currentStreak",
            subtitle = "days",
            icon = "🔥",
            iconColor = StreakFlame
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Best",
            value = "$longestStreak",
            subtitle = "days",
            icon = "🏆",
            iconColor = XpGold
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Total",
            value = "$totalWorkouts",
            subtitle = "workouts",
            icon = "💪",
            iconColor = NeonTeal
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: String,
    iconColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StartWorkoutButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = NeonTeal
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = "START WORKOUT",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun NextWorkoutCard(
    routineName: String,
    routineType: String,
    exerciseCount: Int,
    estimatedMinutes: Int,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Next Workout",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = routineName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "$exerciseCount exercises",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$estimatedMinutes min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TagChip(text = routineType)
                TagChip(text = "Push Day")
            }
        }
    }
}

@Composable
fun TagChip(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = NeonTeal.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = NeonTeal
        )
    }
}

@Composable
fun GymFinderSection(
    gymChains: List<GymChain>,
    selectedChain: GymChain?,
    nearestGym: GymLocation?,
    chainGymLocations: List<GymLocation>,
    userLocation: android.location.Location?,
    isLoading: Boolean,
    error: String?,
    onGymSelected: (GymChain) -> Unit,
    onRequestPermission: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Find Your Gym",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedChain?.displayName ?: "Select Gym Chain"
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    gymChains.forEach { chain ->
                        DropdownMenuItem(
                            text = { Text(chain.displayName) },
                            onClick = {
                                onGymSelected(chain)
                                expanded = false
                            }
                        )
                    }
                }
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = ErrorRed
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonTeal)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enable Location")
                }
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = NeonTeal,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Finding nearest location...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (nearestGym != null) {
                Spacer(modifier = Modifier.height(16.dp))
                NearestGymMap(
                    nearestGym = nearestGym,
                    allGymLocations = chainGymLocations,
                    userLat = userLocation?.latitude,
                    userLng = userLocation?.longitude
                )
            }
        }
    }
}

private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): String {
    val results = FloatArray(1)
    android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
    val distanceMeters = results[0]
    return if (distanceMeters < 1000) {
        "${distanceMeters.toInt()}m"
    } else {
        String.format("%.1fkm", distanceMeters / 1000)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NearestGymMap(
    nearestGym: GymLocation,
    allGymLocations: List<GymLocation>,
    userLat: Double?,
    userLng: Double?
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedGym by remember { mutableStateOf<GymLocation?>(null) }

    Column {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = NeonTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = nearestGym.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = nearestGym.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (userLat != null && userLng != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NeonTeal.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Nearest",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonTeal
                            )
                        }
                    }
                }

                if (userLat != null && userLng != null && nearestGym.latitude != null && nearestGym.longitude != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Distance: ${calculateDistance(userLat, userLng, nearestGym.latitude, nearestGym.longitude)} away",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            AndroidView(
                factory = { ctx ->
                    org.osmdroid.views.MapView(ctx).apply {
                        setMultiTouchControls(true)
                        controller.setZoom(15.0)
                        if (userLat != null && userLng != null) {
                            controller.setCenter(org.osmdroid.util.GeoPoint(userLat, userLng))
                        } else if (nearestGym.latitude != null && nearestGym.longitude != null) {
                            controller.setCenter(org.osmdroid.util.GeoPoint(nearestGym.latitude, nearestGym.longitude))
                        }
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()

                    if (userLat != null && userLng != null) {
                        val userMarker = org.osmdroid.views.overlay.Marker(mapView).apply {
                            position = org.osmdroid.util.GeoPoint(userLat, userLng)
                            setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_CENTER)
                            title = "Your Location"
                            icon = context.getDrawable(android.R.drawable.ic_menu_mylocation)
                        }
                        mapView.overlays.add(userMarker)
                    }

                    allGymLocations.forEach { gym ->
                        if (gym.latitude != null && gym.longitude != null) {
                            val isNearest = gym == nearestGym
                            val marker = org.osmdroid.views.overlay.Marker(mapView).apply {
                                position = org.osmdroid.util.GeoPoint(gym.latitude, gym.longitude)
                                setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                                title = gym.name
                                snippet = gym.address
                                icon = if (isNearest) {
                                    context.getDrawable(context.resources.getIdentifier("ic_nearest_gym", "drawable", context.packageName))
                                } else {
                                    context.getDrawable(context.resources.getIdentifier("ic_gym_marker", "drawable", context.packageName))
                                }
                                setOnMarkerClickListener { _, _ ->
                                    selectedGym = gym
                                    true
                                }
                            }
                            mapView.overlays.add(marker)
                        }
                    }

                    mapView.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )

            if (selectedGym != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedGym!!.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = selectedGym!!.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (userLat != null && userLng != null && selectedGym!!.latitude != null && selectedGym!!.longitude != null) {
                                    Text(
                                        text = calculateDistance(userLat, userLng, selectedGym!!.latitude!!, selectedGym!!.longitude!!) + " away",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            IconButton(onClick = { selectedGym = null }) {
                                Icon(Icons.Default.Clear, contentDescription = "Close")
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                selectedGym?.latitude?.let { lat ->
                                    selectedGym?.longitude?.let { lng ->
                                        val navUrl = "https://www.openstreetmap.org/directions?from=$userLat,$userLng&to=$lat,$lng&route_type=foot"
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(navUrl))
                                        context.startActivity(intent)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                            enabled = userLat != null && userLng != null && selectedGym?.latitude != null
                        ) {
                            Icon(Icons.Default.DirectionsWalk, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Navigate")
                        }
                    }
                }
            }
        }
    }
}