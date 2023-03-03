package com.cumpatomas.brunosrecipes.ui.homefragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Normal
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import coil.compose.rememberAsyncImagePainter
import com.cumpatomas.brunosrecipes.R
import com.cumpatomas.brunosrecipes.components.LoadingAnimation
import com.cumpatomas.brunosrecipes.components.NoInternetMge
import com.cumpatomas.brunosrecipes.data.network.ConnectivityObserver
import com.cumpatomas.brunosrecipes.data.network.NetworkConnectivityObserver
import com.cumpatomas.brunosrecipes.domain.model.NewsModel
import com.cumpatomas.brunosrecipes.domain.model.RecipesModel
import com.cumpatomas.brunosrecipes.manualdi.ApplicationModule.applicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var connectivityObserver: ConnectivityObserver

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.home_fragment, container, false)

        connectivityObserver = NetworkConnectivityObserver(applicationContext)

        view.findViewById<ComposeView>(R.id.composeView).setContent {
        }

        return view
    }

    @SuppressLint("UnsafeRepeatOnLifecycleDetector", "CoroutineCreationDuringComposition")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ComposeView>(R.id.composeView).setContent {

            val internetStatus by connectivityObserver.observe().collectAsState(
                initial = ConnectivityObserver.Status.Unavailable
            )
            val viewModel = viewModel<HomeViewModel>()

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    connectivityObserver = NetworkConnectivityObserver(applicationContext)
                    viewModel.getRecipes()
                    viewModel.closeNewsCard()
                    viewModel.getNews()
                    viewModel.helpSurfaceState.value = false
                }
            }
            HomeScreen(viewModel, internetStatus)
        }
    }

    @SuppressLint("UnrememberedMutableState")
    @Composable
    fun HomeScreen(
        viewModel: HomeViewModel,
        internetStatus: ConnectivityObserver.Status,
        modifier: Modifier = Modifier
    ) {
        val newsestRecipesList = viewModel.newestRecipesList
        val bestRecipesList = viewModel.bestRatedRecipesList

        Column(
            horizontalAlignment = CenterHorizontally,
            modifier = modifier
                .padding(horizontal = 0.dp)
                .verticalScroll(ScrollState(0))
        ) {

            TitleText()

            if (bestRecipesList.value.isNotEmpty()) {
                HomeSection(title = "Tus recetas mejor valoradas") {
                    BestRatedRecipes(bestRecipesList)
                }
            } else {
                HomeSection(title = "Comencemos!") {
                    StartRow()
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (internetStatus == ConnectivityObserver.Status.Unavailable
                || internetStatus == ConnectivityObserver.Status.Lost
            ) {
                if (newsestRecipesList.value.isEmpty()) {

                    // Shows Nothing

                } else {
                    if (viewModel.isLoadingState.value) {
                        HomeSection(title = "Últimas recetas agregadas") {
                            LoadingAnimation(
                                circleColor = colorResource(id = R.color.secondaryColor),
                                circleSize = 12.dp
                            )
                        }

                    } else {
                        HomeSection(title = "Últimas recetas agregadas") {
                            NewestRecipesRow(newsestRecipesList)
                        }
                    }
                }

            } else {
                if (viewModel.isLoadingState.value) {
                    HomeSection(title = "Últimas recetas agregadas") {
                        LoadingAnimation(
                            circleColor = colorResource(id = R.color.white),
                            circleSize = 12.dp
                        )
                    }

                } else {
                    HomeSection(title = "Últimas recetas agregadas") {
                        NewestRecipesRow(newsestRecipesList)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            if (viewModel.isLoadingState.value) {
                HomeSection(title = "Noticias") {

                    LoadingAnimation(
                        circleColor = colorResource(id = R.color.white),
                        circleSize = 12.dp
                    )
                }

            } else {
                HomeSection(title = "Noticias") {
                    NewsColumn(viewModel.newsList.value, internetStatus)
                }
            }
        }
        if (viewModel.helpSurfaceState.value) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row() {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            //.fillMaxHeight(0.8f)
                            .padding(8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = colorResource(id = R.color.superlightgray),
                        elevation = 20.dp,

                        ) {
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            HelpSurfaceCloseButton()
                        }
                        HelpSurfaceItem(viewModel.helpSurfaceText ?: "")
                    }
                }
            }
        }

        if (internetStatus == ConnectivityObserver.Status.Lost
            && newsestRecipesList.value.isEmpty()
        ) {

            viewModel.helpSurfaceState.value = !viewModel.helpSurfaceState.value

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row() {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            //.fillMaxHeight(0.8f)
                            .padding(8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = colorResource(id = R.color.superlightgray),
                        elevation = 20.dp,

                        ) {
/*                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            HelpSurfaceCloseButton()
                        }*/
                        HelpSurfaceItem("Sin conexión de red")
                    }
                }
            }
        }
    }

    private @Composable
    fun HelpSurfaceItem(title: String) {
        when (title) {
            "Buscador de recetas" -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title,
                        fontSize = 25.sp,
                        fontFamily = FontFamily(Font(R.font.beautiful_people)),
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(16.dp),
                    )
                    Text(
                        text = "Usa nuestro buscador de recetas en el Menú/lista.\n\n" +
                                "Puedes buscar por nombre y filtrar las recetas por categorías dulces, saladas, de verano o de invierno.",
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.marlin_sans)),
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(16.dp),
                    )

                    Image(
                        painter = painterResource(id = R.drawable.recipe_list_image),
                        contentDescription = null,
                        contentScale = Crop,
                        modifier = Modifier
                            .size(200.dp, 150.dp)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }


            }
            "Marca tus recetas" -> {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title,
                        fontSize = 25.sp,
                        fontFamily = FontFamily(Font(R.font.beautiful_people)),
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(16.dp),
                    )
                    Text(
                        text = "Puedes marcar tus recetas cada vez que las cocinas usando el botón verde.\n\n" +
                                "Luego en el Menú/Historial tendrás una lista con las recetas y las fechas en que fueron hechas.",
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.marlin_sans)),
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(16.dp),
                    )

                    Image(
                        painter = painterResource(id = R.drawable.mark_your_recipe),
                        contentDescription = null,
                        contentScale = Crop,
                        modifier = Modifier
                            .size(200.dp, 150.dp)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
            "Puntúa tus recetas" -> {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title,
                        fontSize = 25.sp,
                        fontFamily = FontFamily(Font(R.font.beautiful_people)),
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(16.dp),
                    )
                    Text(
                        text = "Puedes puntuar las recetas dándoles estrellas.\n\n" +
                                "Luego podrás encontrar tus recetas mejor valoradas en la pantalla de inicio . ",
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.marlin_sans)),
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(16.dp),
                    )

                    Image(
                        painter = painterResource(id = R.drawable.rate_recipe),
                        contentDescription = null,
                        contentScale = Crop,
                        modifier = Modifier
                            .size(200.dp, 150.dp)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
            "¿Qué puedo cocinar?" -> {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title,
                        fontSize = 25.sp,
                        fontFamily = FontFamily(Font(R.font.beautiful_people)),
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(16.dp),
                    )
                    Text(
                        text = "¿No sabes qué cocinar?\n\n" +
                                "En el Menú/¿qué cocino? podrás ingresar los ingredientes que tienes en casa y obtendrás un listado de posibles recetas.",
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.marlin_sans)),
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(16.dp),
                    )

                    Image(
                        painter = painterResource(id = R.drawable.input_ingredients),
                        contentDescription = null,
                        contentScale = Crop,
                        modifier = Modifier
                            .size(200.dp, 150.dp)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }

            "Sin conexión de red" -> {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title,
                        fontSize = 25.sp,
                        fontFamily = FontFamily(Font(R.font.beautiful_people)),
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(16.dp),
                    )
                    Text(
                        text = "Ups parece que no hay conexión de red.\n\nAlgunas de las funciones en la app pueden no estar disponibles.\n\nVerifica tu conexión.",
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.marlin_sans)),
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(16.dp),
                    )

                    Image(
                        painter = painterResource(id = R.drawable.no_connection),
                        contentDescription = null,
                        contentScale = Crop,
                        modifier = Modifier
                            .size(200.dp, 150.dp)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }
    }


    private @Composable
    fun TitleText() {
        Text(
            text = "- Recetas de Bruno -",
            fontFamily = FontFamily(Font(R.font.beautiful_people)),
            color = colorResource(id = R.color.white),
            textAlign = TextAlign.Center,
            fontSize = 38.sp,
            modifier = Modifier
                .paddingFromBaseline(top = 24.dp)
                .padding(horizontal = 8.dp, vertical = 16.dp)
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    fun NewsWebView(new: NewsModel) {
        val loadingState = remember { mutableStateOf(true) }
        val coroutineScope = rememberCoroutineScope()

        Surface(
            color = Color.White,
            modifier = Modifier
                .height(450.dp)
                .verticalScroll(rememberScrollState())
        ) {

            AndroidView(
                modifier = Modifier.background(Color.White),
                factory = {
                    WebView(it).apply {
                        coroutineScope.launch {
                            val job = launch {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                webViewClient = WebViewClient()
                                settings.javaScriptEnabled = true
                                loadUrl(new.link)

                            }
                            job.join()
                            delay(2000)
                            loadingState.value = false
                        }
                    }
                }, /*update = {
                it.loadUrl(new.link)
            }*/
            )

        }
        if (loadingState.value) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                LoadingAnimation(
                    circleColor = colorResource(id = R.color.secondaryColor),
                    circleSize = 12.dp,
                    modifier = Modifier
                        .alpha(0.7f)
                        .padding(top = 24.dp)
                )
            }

        } else
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.padding(8.dp)
            ) {
                NewsCloseButton(new)
            }
    }

    @Composable
    fun NewsCloseButton(
        new: NewsModel,
    ) {
        val coroutineScope = rememberCoroutineScope()
        FloatingActionButton(
            backgroundColor = colorResource(id = R.color.secondaryColor),
            onClick = {
                coroutineScope.launch {
                    new.state.value = false
                }
            },
            modifier = Modifier
                .size(50.dp)
                .alpha(0.6f)
        )
        {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Volver",
                tint = colorResource(id = R.color.white),
                modifier = Modifier.padding(0.dp)
            )
        }
    }

    @Composable
    fun HelpSurfaceCloseButton(
        viewModel: HomeViewModel = viewModel()
    ) {
        val coroutineScope = rememberCoroutineScope()
        FloatingActionButton(
            backgroundColor = colorResource(id = R.color.secondaryColor),
            onClick = {
                coroutineScope.launch {
                    viewModel.helpSurfaceState.value = false
                }
            },
            modifier = Modifier
                .size(40.dp)
                .alpha(0.5f)
        )
        {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Volver",
                tint = colorResource(id = R.color.white),
                modifier = Modifier.padding(0.dp)
            )
        }
    }

    @Composable
    fun NewsColumn(
        newsList: List<NewsModel>,
        internetStatus: ConnectivityObserver.Status,
        viewModel: HomeViewModel = viewModel()
    ) {

        Column(
            horizontalAlignment = CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        )

        {
            if (newsList.isNotEmpty()) {
                YukaCard()
            }
            if (internetStatus == ConnectivityObserver.Status.Unavailable
                || internetStatus == ConnectivityObserver.Status.Lost
            ) {
                if (newsList.isEmpty()) {
                    NoInternetMge()
                }
            } else {
                if (newsList.isNotEmpty()) {
                    for (i in 0..9)
                        NewsItem(newsList[i])
                }

            }
        }
    }

    @Composable
    private fun YukaCard() {
        val YukaWebViewState = remember { mutableStateOf(false) }

        Card(
            elevation = 8.dp,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 16.dp)
                .clickable {
                    YukaWebViewState.value = !YukaWebViewState.value
                }
        ) {
            if (YukaWebViewState.value)
                YucaWebView(YukaWebViewState)
            else {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Prueba la app de",
                        fontFamily = FontFamily(Font(R.font.marlin_sans)),
                        color = colorResource(id = R.color.mid_gray),
                        textAlign = TextAlign.Center,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
//                            .paddingFromBaseline(top = 28.dp)
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.yuka_logo),
                        contentDescription = "Yuka",
                    )
                    Text(
                        text = "gratis!",
                        fontFamily = FontFamily(Font(R.font.marlin_sans)),
                        color = colorResource(id = R.color.secondaryColor),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .rotate(-35f)
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    private fun YucaWebView(YukaWebViewState: MutableState<Boolean>) {
        val coroutineScope = rememberCoroutineScope()
        val loadingState = remember { mutableStateOf(true) }
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .padding(horizontal = 0.dp)
                .padding(bottom = 8.dp)
                .height(450.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())


        ) {

            AndroidView(
                factory = {
                    WebView(it).apply {
                        coroutineScope.launch {
                            val job = launch {

                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                webViewClient = WebViewClient()
                                settings.javaScriptEnabled = true
                                loadUrl("https://yuka.io/es/aplicacion/")
                            }
                            job.join()
                            delay(3000)
                            loadingState.value = false
                        }

                    }
                },
                update = {
                    it.loadUrl("https://yuka.io/es/aplicacion/")
                }

            )
        }
        if (loadingState.value) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                LoadingAnimation(
                    circleColor = colorResource(id = R.color.secondaryColor),
                    circleSize = 12.dp,
                    modifier = Modifier
                        .alpha(0.7f)
                        .padding(top = 24.dp)
                )
            }

        } else {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.padding(8.dp)
            ) {

                FloatingActionButton(
                    backgroundColor = colorResource(id = R.color.secondaryColor),
                    onClick = {
                        coroutineScope.launch {
                            YukaWebViewState.value = false
                        }
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .alpha(0.5f)
                )
                {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Volver",
                        tint = colorResource(id = R.color.white),
                        modifier = Modifier.padding(0.dp)
                    )
                }
            }
        }
    }

    @Composable
    private fun NewsItem(
        new: NewsModel,
    ) {
        Card(
            elevation = 8.dp,
            shape = RoundedCornerShape(10.dp),
            backgroundColor = colorResource(id = R.color.white),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 16.dp)
        ) {
            Column(verticalArrangement = Arrangement.Center) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = new.title,
                        fontFamily = FontFamily(Font(R.font.marlin_sans)),
                        color = colorResource(id = R.color.mid_gray),
                        textAlign = TextAlign.Center,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .paddingFromBaseline(top = 24.dp)
                            .padding(horizontal = 8.dp)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "leer...",
                        fontFamily = FontFamily(Font(R.font.marlin_sans)),
                        color = colorResource(id = R.color.secondaryColor),
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .paddingFromBaseline(bottom = 8.dp)
                            .padding(horizontal = 8.dp)
                            .clickable {

                                new.state.value = true
                            }
                    )
                }
            }
            if (new.state.value)
                NewsWebView(new)
        }
    }

    @Composable
    private fun StartRow() {
        val lazyState = rememberLazyListState(initialFirstVisibleItemIndex = 1)

        val centerPosition by remember { // caching position for prevent recomposition
            derivedStateOf {
                val visibleInfo = lazyState.layoutInfo.visibleItemsInfo
                if (visibleInfo.isEmpty()) -1
                else {
                    //TODO: enhance calculate logic for specific position
//                    val offset = (visibleInfo.last().index - visibleInfo.first().index) / 2
                    visibleInfo.first().index + 1
                }
            }
        }
        LazyRow(
            verticalAlignment = Alignment.Top,
            state = lazyState,
            modifier = Modifier
                .fillMaxWidth()
        ) {

            val SratRowElementsTexts = listOf<Pair<String, Int>>(
                Pair(" ", R.drawable.recipe_list_image),
                Pair("Puntúa tus recetas", R.drawable.rate_recipe),
                Pair("Buscador de recetas", R.drawable.recipe_list_image),
                Pair("¿Qué puedo cocinar?", R.drawable.input_ingredients),
                Pair("Marca tus recetas", R.drawable.mark_your_recipe),
                Pair("Puntúa tus recetas", R.drawable.rate_recipe),
                Pair("Buscador de recetas", R.drawable.recipe_list_image),
                Pair("¿Qué puedo cocinar?", R.drawable.input_ingredients),
                Pair("Marca tus recetas", R.drawable.mark_your_recipe),
                Pair(" ", R.drawable.recipe_list_image),
            )


            itemsIndexed(SratRowElementsTexts) { index, textAndImage ->
                StartRowElement(
                    textAndImage.first,
                    textAndImage.second,
                    expanded = index == centerPosition
                )
            }
        }
    }

    @Composable
    private fun StartRowElement(
        text: String,
        @DrawableRes drawable: Int,
        viewModel: HomeViewModel = viewModel(),
        expanded: Boolean
    ) {

        Column(
            horizontalAlignment = CenterHorizontally,
            modifier = Modifier
                .padding(end = 8.dp)
                .padding(top = 0.dp, bottom = 16.dp)
                .width(110.dp)
                .height(150.dp)
        ) {

            Surface(
                elevation = if (text.isBlank()) 0.dp else 14.dp,
                color = Color.Transparent,
                shape = CircleShape,
                modifier = Modifier
                    .clickable {
                        viewModel.helpSurfaceText = text
                        viewModel.helpSurfaceState.value = true
                    }
            ) {

                if (text.isBlank()) {
                    Image(
                        painter = painterResource(id = drawable),
                        contentDescription = null,
                        contentScale = Crop,
                        alpha = 0f,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Image(
                        painter = painterResource(id = drawable),
                        contentDescription = null,
                        contentScale = Crop,
                        modifier = Modifier
                            .animateContentSize(
                                animationSpec = tween(
                                    durationMillis = 400,
                                    easing = LinearOutSlowInEasing
                                )
                            )
                            .size(if (expanded) 110.dp else 80.dp)
                            //.clip(RoundedCornerShape(16.dp))
                            .clip(CircleShape)
                    )
                }


            }

            Text(
                text = text,
                fontFamily = FontFamily(Font(R.font.marlin_sans)),
                color = colorResource(id = R.color.white),
                textAlign = TextAlign.Center,
                fontSize = if (expanded) 16.sp else 13.sp,
                fontWeight = if (expanded) FontWeight.Bold else Normal,
                modifier = Modifier
                    .animateContentSize(
                        animationSpec = tween(
                            durationMillis = 400,
                            easing = LinearOutSlowInEasing
                        )
                    )
                    .paddingFromBaseline(20.dp)

            )
        }
    }

    @Composable
    fun BestRatedRecipes(
        bestRecipesList: State<List<RecipesModel>>,
        modifier: Modifier = Modifier,
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(0.dp),
            modifier = modifier
        ) {
            items(bestRecipesList.value) { recipe ->
                BestRatedCard(
                    photo = recipe.photo,
                    text = recipe.name,
                    rate = recipe.rating,
                    id = recipe.id
                )
            }
        }
    }

    @Composable
    private fun BestRatedCard(
        photo: String,
        text: String,
        rate: Float,
        id: Int,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = Modifier
                .width(180.dp)
                .padding(horizontal = 8.dp)
                .padding(bottom = 16.dp, top = 8.dp)
                .clickable {
                    val action = HomeFragmentDirections.actionHomeFragmentToRecipeFragment(id)
                    findNavController().navigate(action)
                },
            elevation = 12.dp,
            shape = RoundedCornerShape(16.dp)

        ) {
            Column(
                horizontalAlignment = CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = modifier.padding(vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when (rate) {
                            1.0f -> "1/5"
                            2.0f ->
                                "2/5"
                            3.0f ->
                                "3/5"
                            4.0f ->
                                "4/5"
                            else ->
                                "5/5"
                        },
                        fontSize = 20.sp,
                        color = colorResource(id = R.color.superlightgreen),
                        fontFamily = FontFamily(Font(R.font.marlin_sans)),
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = colorResource(id = R.color.superlightgreen),
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Image(
                    painter = rememberAsyncImagePainter(photo),
                    contentDescription = null,
                    contentScale = Crop,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                )
                Text(
                    text = text,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily(Font(R.font.marlin_sans))
                )
            }

        }
    }

    @Composable
    private fun NewestRecipesRow(
        recipesList: State<List<RecipesModel>>,
        modifier: Modifier = Modifier
    ) {
        Surface(
            elevation = 14.dp,
        )
        {
            LazyRow(
                modifier = modifier
                    .background(color = colorResource(id = R.color.white))
                    .padding(vertical = 16.dp)
                    .height(140.dp),

                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(recipesList.value) { recipe ->
                    NewestRecipesElement(
                        photo = recipe.photo,
                        text = recipe.name,
                        id = recipe.id

                    )
                }
            }
        }
    }

    @Composable
    private fun NewestRecipesElement(
        photo: String,
        text: String,
        id: Int,
        modifier: Modifier = Modifier
    ) {
        Column(
            horizontalAlignment = CenterHorizontally,
            modifier = modifier
                .width(100.dp)
                .clickable {
                    val action = HomeFragmentDirections.actionHomeFragmentToRecipeFragment(id)
                    findNavController().navigate(action)
                },
        ) {

            Image(
                painter = rememberAsyncImagePainter(photo),
                contentDescription = null,
                contentScale = Crop,
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
            )
            Text(
                text = text,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily(Font(R.font.marlin_sans)),
                modifier = Modifier.paddingFromBaseline(top = 24.dp)
            )
        }
    }

    @Composable
    fun HomeSection(title: String, content: @Composable () -> Unit) {

        Text(
            text = title,
            fontFamily = FontFamily(Font(R.font.beautiful_people)),
            color = colorResource(id = R.color.white),
            textAlign = TextAlign.Center,
            fontSize = 25.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}

