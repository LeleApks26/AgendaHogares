package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AgendaTab
import com.example.ui.AgendaViewModel
import com.example.ui.components.BottomActionBar
import com.example.ui.components.CatalogItemDialog
import com.example.ui.components.ClearDayDialog
import com.example.ui.components.DatePickerModal
import com.example.ui.components.EditEntryDialog
import com.example.ui.components.ExportJpgDialog
import com.example.ui.components.HeaderBar
import com.example.ui.components.LogoExpandDialog
import com.example.ui.components.SavedAgendasDialog
import com.example.ui.components.SplashScreen
import com.example.ui.components.SubHeaderBar
import com.example.ui.components.WhatsAppShareDialog
import com.example.ui.screens.ActividadesScreen
import com.example.ui.screens.ChicosScreen
import com.example.ui.screens.CronogramaScreen
import com.example.ui.screens.LugaresScreen
import com.example.ui.screens.OperadoresScreen
import com.example.ui.screens.PlanillaScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AgendaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDarkMode) {
                AgendaApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AgendaApp(viewModel: AgendaViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val currentDateKey by viewModel.currentDateKey.collectAsStateWithLifecycle()
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val savedDates by viewModel.savedDates.collectAsStateWithLifecycle()
    val chicos by viewModel.chicos.collectAsStateWithLifecycle()
    val actividades by viewModel.actividades.collectAsStateWithLifecycle()
    val lugares by viewModel.lugares.collectAsStateWithLifecycle()
    val operadores by viewModel.operadores.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    // Dialog states
    val isEditEntryDialogOpen by viewModel.isEditEntryDialogOpen.collectAsStateWithLifecycle()
    val editingEntry by viewModel.editingEntry.collectAsStateWithLifecycle()
    val isWhatsAppDialogOpen by viewModel.isWhatsAppDialogOpen.collectAsStateWithLifecycle()
    val isExportJpgDialogOpen by viewModel.isExportJpgDialogOpen.collectAsStateWithLifecycle()
    val exportedJpgFile by viewModel.exportedJpgFile.collectAsStateWithLifecycle()
    val isSavedAgendasDialogOpen by viewModel.isSavedAgendasDialogOpen.collectAsStateWithLifecycle()
    val weekdayTemplates by viewModel.weekdayTemplates.collectAsStateWithLifecycle()
    val isClearDayDialogOpen by viewModel.isClearDayDialogOpen.collectAsStateWithLifecycle()
    val isDatePickerDialogOpen by viewModel.isDatePickerDialogOpen.collectAsStateWithLifecycle()
    val isCatalogDialogOpen by viewModel.isCatalogDialogOpen.collectAsStateWithLifecycle()
    val catalogEditType by viewModel.catalogEditType.collectAsStateWithLifecycle()
    val editingChico by viewModel.editingChico.collectAsStateWithLifecycle()
    val editingActividad by viewModel.editingActividad.collectAsStateWithLifecycle()
    val editingLugar by viewModel.editingLugar.collectAsStateWithLifecycle()
    val editingOperador by viewModel.editingOperador.collectAsStateWithLifecycle()

    var showSplashScreen by rememberSaveable { mutableStateOf(true) }
    var isLogoExpanded by rememberSaveable { mutableStateOf(false) }

    if (showSplashScreen) {
        SplashScreen(onFinish = { showSplashScreen = false })
        return
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        topBar = {
            Column {
                HeaderBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.setTab(it) },
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onLogoClick = { isLogoExpanded = true }
                )
                // SubHeader with date navigation & static width and + Fila button
                SubHeaderBar(
                    currentDateKey = currentDateKey,
                    recordCount = entries.size,
                    onPrevDay = { viewModel.prevDay() },
                    onNextDay = { viewModel.nextDay() },
                    onOpenDatePicker = { viewModel.isDatePickerDialogOpen.value = true },
                    onOpenSavedAgendas = { viewModel.openSavedAgendasDialog() },
                    onAddRowClick = { viewModel.openAddEntryDialog() }
                )
            }
        },
        bottomBar = {
            BottomActionBar(
                onExportJpgToWhatsApp = { viewModel.exportPlanillaToJpg() },
                onClearDayClick = { viewModel.isClearDayDialogOpen.value = true }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AgendaTab.PLANILLA -> PlanillaScreen(
                    entries = entries,
                    chicosCatalog = chicos,
                    operadoresCatalog = operadores,
                    onEditEntry = { viewModel.openEditEntryDialog(it) },
                    onDuplicateEntry = { viewModel.duplicateEntry(it) },
                    onDeleteEntry = { viewModel.deleteEntry(it) },
                    onAddRowClick = { viewModel.openAddEntryDialog() },
                    isDarkMode = isDarkMode
                )
                AgendaTab.CRONOGRAMA -> CronogramaScreen(
                    entries = entries,
                    chicosCatalog = chicos,
                    operadoresCatalog = operadores,
                    onEditEntry = { viewModel.openEditEntryDialog(it) },
                    onDuplicateEntry = { viewModel.duplicateEntry(it) },
                    onDeleteEntry = { viewModel.deleteEntry(it) }
                )
                AgendaTab.CHICOS -> ChicosScreen(
                    chicos = chicos,
                    onAddChico = { viewModel.openAddChicoDialog() },
                    onEditChico = { viewModel.openEditChicoDialog(it) },
                    onDeleteChico = { viewModel.deleteChico(it) }
                )
                AgendaTab.ACTIVIDADES -> ActividadesScreen(
                    actividades = actividades,
                    onAddActividad = { viewModel.openAddActividadDialog() },
                    onDeleteActividad = { viewModel.deleteActividad(it) }
                )
                AgendaTab.LUGARES -> LugaresScreen(
                    lugares = lugares,
                    onAddLugar = { viewModel.openAddLugarDialog() },
                    onDeleteLugar = { viewModel.deleteLugar(it) }
                )
                AgendaTab.OPERADORES -> OperadoresScreen(
                    operadores = operadores,
                    onAddOperador = { viewModel.openAddOperadorDialog() },
                    onEditOperador = { viewModel.openEditOperadorDialog(it) },
                    onDeleteOperador = { viewModel.deleteOperador(it) }
                )
            }
        }
    }

    // Dialogs
    if (isEditEntryDialogOpen) {
        EditEntryDialog(
            entry = editingEntry,
            chicosCatalog = chicos,
            actividadesCatalog = actividades,
            lugaresCatalog = lugares,
            operadoresCatalog = operadores,
            onAddChico = { viewModel.quickAddChico(it) },
            onAddActividad = { viewModel.quickAddActividad(it) },
            onAddLugar = { viewModel.quickAddLugar(it) },
            onAddOperador = { viewModel.quickAddOperador(it) },
            onDismiss = { viewModel.closeEditEntryDialog() },
            onSave = { id, time, ch, act, lug, resp, obs ->
                viewModel.saveEntry(id, time, ch, act, lug, resp, obs)
            }
        )
    }

    if (isExportJpgDialogOpen) {
        ExportJpgDialog(
            file = exportedJpgFile,
            dateKey = currentDateKey,
            onDismiss = { viewModel.isExportJpgDialogOpen.value = false }
        )
    }

    if (isWhatsAppDialogOpen) {
        WhatsAppShareDialog(
            messageText = viewModel.formatWhatsAppMessage(),
            onDismiss = { viewModel.isWhatsAppDialogOpen.value = false }
        )
    }

    if (isSavedAgendasDialogOpen) {
        SavedAgendasDialog(
            savedDates = savedDates,
            currentDate = currentDateKey,
            weekdayTemplates = weekdayTemplates,
            onSelectDate = { viewModel.setDateKey(it) },
            onLoadWeekdayTemplate = { viewModel.loadWeekdayTemplateIntoCurrentDay(it) },
            onSaveWeekdayTemplate = { viewModel.saveCurrentDayAsWeekdayTemplate(it) },
            onDismiss = { viewModel.isSavedAgendasDialogOpen.value = false }
        )
    }

    if (isDatePickerDialogOpen) {
        DatePickerModal(
            onDateSelected = {
                viewModel.setDateKey(it)
                viewModel.isDatePickerDialogOpen.value = false
            },
            onDismiss = { viewModel.isDatePickerDialogOpen.value = false }
        )
    }

    if (isClearDayDialogOpen) {
        ClearDayDialog(
            dateKey = currentDateKey,
            onConfirm = { viewModel.clearCurrentDay() },
            onDismiss = { viewModel.isClearDayDialogOpen.value = false }
        )
    }

    if (isCatalogDialogOpen) {
        CatalogItemDialog(
            type = catalogEditType,
            editingChico = editingChico,
            editingActividad = editingActividad,
            editingLugar = editingLugar,
            editingOperador = editingOperador,
            onDismiss = { viewModel.isCatalogDialogOpen.value = false },
            onSaveChico = { name, color, textCol -> viewModel.saveChico(name, color, textCol) },
            onSaveActividad = { name, color -> viewModel.saveActividad(name, color) },
            onSaveLugar = { name -> viewModel.saveLugar(name) },
            onSaveOperador = { name, color -> viewModel.saveOperador(name, color) }
        )
    }

    if (isLogoExpanded) {
        LogoExpandDialog(
            onDismiss = { isLogoExpanded = false }
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(text = "Agenda Hogares: $name", modifier = modifier)
}

