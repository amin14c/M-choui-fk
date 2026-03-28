package com.smsscheduler.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.smsscheduler.data.SmsSchedule
import com.smsscheduler.viewmodel.SmsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleScreen(
    navController: NavController,
    scheduleId: Int? = null,
    viewModel: SmsViewModel = viewModel()
) {
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()
    val existingSchedule = scheduleId?.let { id -> schedules.find { it.id == id } }

    var recipientName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf("8") }
    var minute by remember { mutableStateOf("0") }

    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var messageError by remember { mutableStateOf(false) }
    var timeError by remember { mutableStateOf(false) }

    val isEditing = scheduleId != null

    LaunchedEffect(existingSchedule) {
        existingSchedule?.let {
            recipientName = it.recipientName
            phoneNumber = it.phoneNumber
            message = it.message
            hour = it.hour.toString()
            minute = it.minute.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "تعديل الجدول" else "جدول جديد") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                nameError = recipientName.isBlank()
                phoneError = phoneNumber.isBlank()
                messageError = message.isBlank()
                val h = hour.toIntOrNull()
                val m = minute.toIntOrNull()
                timeError = h == null || m == null || h !in 0..23 || m !in 0..59

                if (!nameError && !phoneError && !messageError && !timeError) {
                    val schedule = SmsSchedule(
                        id = scheduleId ?: 0,
                        recipientName = recipientName.trim(),
                        phoneNumber = phoneNumber.trim(),
                        message = message.trim(),
                        hour = h!!,
                        minute = m!!,
                        isActive = existingSchedule?.isActive ?: true
                    )
                    if (isEditing) viewModel.updateSchedule(schedule)
                    else viewModel.addSchedule(schedule)
                    navController.popBackStack()
                }
            }) {
                Icon(Icons.Default.Check, contentDescription = "حفظ")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = recipientName, onValueChange = { recipientName = it; nameError = false },
                label = { Text("اسم المستلم") }, placeholder = { Text("مثال: أمي، المستشفى...") },
                isError = nameError, supportingText = if (nameError) { { Text("هذا الحقل مطلوب") } } else null,
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = phoneNumber, onValueChange = { phoneNumber = it; phoneError = false },
                label = { Text("رقم الهاتف") }, placeholder = { Text("مثال: +213xxxxxxxxx") },
                isError = phoneError, supportingText = if (phoneError) { { Text("هذا الحقل مطلوب") } } else null,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true
            )
            OutlinedTextField(
                value = message, onValueChange = { message = it; messageError = false },
                label = { Text("نص الرسالة") }, placeholder = { Text("اكتب الرسالة هنا...") },
                isError = messageError, supportingText = if (messageError) { { Text("هذا الحقل مطلوب") } } else null,
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                maxLines = 6, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            Text("وقت الإرسال اليومي", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = hour, onValueChange = { if (it.length <= 2) { hour = it; timeError = false } },
                    label = { Text("الساعة") }, placeholder = { Text("0-23") }, isError = timeError,
                    modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true
                )
                OutlinedTextField(
                    value = minute, onValueChange = { if (it.length <= 2) { minute = it; timeError = false } },
                    label = { Text("الدقيقة") }, placeholder = { Text("0-59") }, isError = timeError,
                    modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true
                )
            }
            if (timeError) {
                Text("يرجى إدخال وقت صحيح (الساعة: 0-23، الدقيقة: 0-59)", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}
