package cl.duoc.activaciones.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.activaciones.viewmodel.LoginViewModel
import cl.duoc.activaciones.ui.theme.DegradadoRosaMorado
import cl.duoc.activaciones.ui.components.app
import cl.duoc.activaciones.ui.components.vmFactory

@Composable
fun LoginScreen(alEntrar: () -> Unit, alRegistrar: () -> Unit) {
    val app = app()
    val vm: LoginViewModel = viewModel(factory = vmFactory { LoginViewModel(app.repo, app.sesion) })
    val error by vm.error.collectAsState()
    var correo by rememberSaveable { mutableStateOf("ejecutivo@demo.cl") }
    var clave by rememberSaveable { mutableStateOf("1234") }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Activaciones", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Box(Modifier.padding(vertical = 6.dp).width(80.dp).height(4.dp).background(DegradadoRosaMorado))
        Text("Gestión de oportunidades corporativas", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(correo, { correo = it }, label = { Text("Correo") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            clave, { clave = it }, label = { Text("Contraseña") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()
        )
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { vm.ingresar(correo, clave, alEntrar) }, modifier = Modifier.fillMaxWidth()) { Text("Ingresar") }
        TextButton(onClick = alRegistrar) { Text("Crear cuenta") }
        Spacer(Modifier.height(16.dp))
        Text("Demo: ejecutivo@demo.cl o supervisor@demo.cl · clave 1234", style = MaterialTheme.typography.bodySmall)
    }
}