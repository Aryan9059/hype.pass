package com.pass.hype.presentation.components

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.pass.hype.R
import com.pass.hype.data.local.passwords.Passwords
import com.pass.hype.presentation.passwords.PasswordViewModel
import com.pass.hype.utils.appList
import com.pass.hype.utils.capitalizeWords
import kotlinx.coroutines.launch

@SuppressLint("DiscouragedApi")
@Composable
fun PasswordItem(
    item: Passwords,
    modifier: Modifier
) {
    val context = LocalContext.current
    val vm = viewModel<PasswordViewModel>()
    val drawableId = remember(item.appIcon) {
        context.resources.getIdentifier(
            item.appIcon,
            "drawable",
            context.packageName
        )
    }

    var isPasswordDialogOpen by rememberSaveable { mutableStateOf(false) }
    var app by remember { mutableStateOf(item.appName) }
    var email by remember { mutableStateOf(item.email) }
    var password by remember { mutableStateOf(item.password) }

    AddPasswordDialog(
        edit = true,
        isOpen = isPasswordDialogOpen,
        app = app,
        email = email,
        password = password,
        onDismissRequest = {
            isPasswordDialogOpen = false
            email = item.email
            password = item.password
            app = item.appName },
        onConfirmButtonClick = {
            vm.addPassword(
                Passwords(
                    appName = app,
                    appIcon = app.lowercase(),
                    email = email,
                    password = password,
                    editTime = System.currentTimeMillis().toString(),
                    edited = true
                )
            )
            vm.deletePassword(item.passwordId)
            isPasswordDialogOpen = false },
        onEmailChanged = {email = it},
        onPasswordChanged = {password = it},
        onAppChanged = {app = it}
    )

    val alphaAnimation = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(100) {
        alphaAnimation.animateTo(targetValue = 1f, animationSpec = tween(300, 0))
    }

    var expandedState by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var isDeleteDialogOpen by rememberSaveable { mutableStateOf(false) }

    DeleteDialog(isOpen = isDeleteDialogOpen, item = "Password", onDelete = {
        scope.launch {
            vm.deletePassword(item.passwordId)
            isDeleteDialogOpen = false
        }
    }, onDismissRequest = { isDeleteDialogOpen = false })

    OutlinedCard(
        shape = RoundedCornerShape(21.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 10.dp)
            .graphicsLayer {
                alpha = alphaAnimation.value
            }
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ),
        onClick = { expandedState = !expandedState }
    ) {
        val clipboardManager = LocalClipboardManager.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Card (
                    modifier = Modifier
                        .height(32.dp)
                        .width(32.dp)
                        .align(Alignment.CenterVertically),
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ){
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (!appList.contains(item.appName)){
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                text = item.appName.first().toString(),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        } else{
                            Icon(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .align(Alignment.Center)
                                    .padding(6.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                painter = painterResource(id = drawableId),
                                contentDescription = "Social Media Icon")
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                        .align(Alignment.CenterVertically),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.appName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        modifier = Modifier.padding(end = 16.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        text = if (expandedState) {
                            if (item.edited) "Edited ${TimeAgo.using(item.editTime.toLong()).capitalizeWords()}" else "Created ${TimeAgo.using(item.editTime.toLong()).capitalizeWords()}"
                        } else item.email,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                OutlinedCard(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, "App Name: ${item.appName}\nEmail: ${item.email}\nPassword: ${item.password}")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        startActivity(context, shareIntent, null)
                    },
                    modifier = Modifier.align(Alignment.CenterVertically),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(modifier = Modifier
                        .size(36.dp)
                        .padding(8.dp),
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Password")
                }
                OutlinedCard(onClick = {
                    isPasswordDialogOpen = true
                },
                    modifier = Modifier.align(Alignment.CenterVertically),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(modifier = Modifier
                        .size(36.dp)
                        .padding(8.dp),
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Password")
                }
            }

            if(expandedState){
                OutlinedTextField(
                    value = item.email,
                    onValueChange = {
                        item.email = it},
                    label = { Text(text = "Email/UserID")},
                    singleLine = true,
                    readOnly = true,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    maxLines = 1,
                    trailingIcon = {
                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(item.email))
                        }) {
                            Icon(imageVector = Icons.Default.CopyAll, contentDescription = "Copy Email")
                        }
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.AlternateEmail, contentDescription = "Email")
                    }
                )

                var passwordVisibility: Boolean by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = item.password,
                    onValueChange = {item.password = it},
                    label = { Text(text = "Password")},
                    readOnly = true,
                    visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = FontFamily(Font(R.font.password))),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passwordVisibility)
                            Icons.Default.VisibilityOff
                        else Icons.Default.Visibility

                        val description = if (passwordVisibility) "Hide Password" else "Show Password"

                        IconButton(onClick = {passwordVisibility = !passwordVisibility}){
                            Icon(imageVector = image, description)
                        }
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Password, contentDescription = "Password Text Box Icon")
                    }
                )
            }

            Row (modifier = Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(8.dp)){
                OutlinedButton(onClick = {
                    isDeleteDialogOpen = true
                },
                    modifier = Modifier
                        .padding(top = 8.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Text(text = "Delete", color = MaterialTheme.colorScheme.error)
                }

                Button(onClick = { clipboardManager.setText(AnnotatedString(item.password)) },
                    modifier = Modifier
                        .padding(top = 8.dp)) {
                    Text(text = "Copy")
                }
            }
        }
    }
}