package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.models.AgendaEntry
import com.example.data.models.Chico
import com.example.data.models.Operador
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object PlanillaImageGenerator {

    private fun parseColor(hex: String, defaultColor: Int): Int {
        return try {
            val clean = hex.removePrefix("#")
            val colorLong = clean.toLong(16)
            if (clean.length == 6) {
                (0xFF000000 or colorLong).toInt()
            } else {
                colorLong.toInt()
            }
        } catch (_: Exception) {
            defaultColor
        }
    }

    /**
     * Splits or wraps text into two lines (doble renglón) if it contains
     * any word with >12 characters or if total length exceeds 12 characters.
     */
    fun formatDoubleLineIfLong(text: String, maxCharsPerLine: Int = 12): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return ""
        val words = trimmed.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val hasLongWord = words.any { it.length > maxCharsPerLine }
        if (!hasLongWord && trimmed.length <= maxCharsPerLine) {
            return trimmed
        }

        if (words.size > 1) {
            val lines = mutableListOf<String>()
            var currentLine = ""
            for (w in words) {
                if (currentLine.isEmpty()) {
                    currentLine = w
                } else if ((currentLine.length + 1 + w.length) <= maxCharsPerLine) {
                    currentLine = "$currentLine $w"
                } else {
                    lines.add(currentLine)
                    currentLine = w
                }
            }
            if (currentLine.isNotBlank()) {
                lines.add(currentLine)
            }
            return lines.joinToString("\n")
        } else {
            val chunks = trimmed.chunked(maxCharsPerLine)
            return chunks.joinToString("-\n")
        }
    }

    /**
     * Generates a high-quality JPG image tightly adapted to vertical mobile screens.
     * Columns are snug and narrow to avoid wasting horizontal space.
     * Actividad and Lugar wrap to double lines if words > 12 chars.
     * Observaciones column width is strictly limited to the width of the word "OBSERVACIONES".
     * Chicos chips have maximum 2 per line and tight 1-char padding.
     * Operadores are stacked vertically one below the other with tight 1-char padding.
     * Canvas clipping prevents any text from ever spilling into adjacent columns.
     */
    fun generatePlanillaJpg(
        context: Context,
        dateKey: String,
        entries: List<AgendaEntry>,
        chicosCatalog: List<Chico>,
        operadoresCatalog: List<Operador>
    ): File? {
        try {
            val chicosColorMap = chicosCatalog.associate {
                it.name.uppercase() to Pair(
                    parseColor(it.colorHex, AndroidColor.parseColor("#3B82F6")),
                    parseColor(it.textColorHex, AndroidColor.WHITE)
                )
            }
            val operadoresColorMap = operadoresCatalog.associate {
                it.name.uppercase() to Pair(
                    parseColor(it.colorHex, AndroidColor.parseColor("#2563EB")),
                    parseColor(it.textColorHex, AndroidColor.WHITE)
                )
            }

            val marginLeft = 10f
            val marginRight = 10f

            // Text paints for measuring and rendering
            val tableHeaderPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = AndroidColor.WHITE
            }

            // Strictly pure black text with uniform BOLD weight for Actividad, Lugar, and Observaciones
            val actTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                textSize = 13.5f
                color = AndroidColor.BLACK
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val lugarTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                textSize = 13.5f
                color = AndroidColor.BLACK
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val obsTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                textSize = 13.5f
                color = AndroidColor.BLACK
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            // =========================================================================
            // DYNAMIC AUTO-FIT RULE (AUTOAJUSTE DE COLUMNAS AL MÍNIMO POSIBLE):
            // Adjusts each column width to the minimum possible based on cell text content
            // to make the exported image as narrow as possible for vertical mobile viewing.
            // =========================================================================

            val chipCharWidth = 13f * 0.45f
            val chipPadX = chipCharWidth.coerceIn(4.5f, 6.5f)
            val chipGapX = 4f

            // 0: # (Index)
            val indexHeaderW = tableHeaderPaint.measureText("#")
            val maxNumW = actTextPaint.measureText("${entries.size.coerceAtLeast(10)}")
            val col0W = (maxOf(indexHeaderW, maxNumW) + 12f).coerceAtLeast(24f)

            // 1: HORA
            val horaHeaderW = tableHeaderPaint.measureText("HORA ▲")
            var maxTimeW = 0f
            for (item in entries) {
                val tw = actTextPaint.measureText(item.time)
                if (tw > maxTimeW) maxTimeW = tw
            }
            val timePillW = (maxTimeW + 14f).coerceAtLeast(50f)
            val col1W = maxOf(horaHeaderW + 10f, timePillW + 6f)

            // 2: NIÑA/O (Max 2 children per line; measure widest pair of chips)
            val ninaHeaderW = tableHeaderPaint.measureText("NIÑA/O")
            var maxChicosPairW = 0f
            for (item in entries) {
                val list = item.chicos.split(",").map { it.trim() }.filter { it.isNotBlank() }
                list.chunked(2).forEach { pair ->
                    val pairW = pair.sumOf { name ->
                        (actTextPaint.measureText(name) + chipPadX * 2).toDouble()
                    }.toFloat() + (pair.size - 1) * chipGapX
                    if (pairW > maxChicosPairW) maxChicosPairW = pairW
                }
            }
            val col2W = maxOf(ninaHeaderW + 12f, maxChicosPairW + 10f).coerceAtLeast(70f)

            // 3: ACTIVIDAD (Wraps long words, fits longest token)
            val actHeaderW = tableHeaderPaint.measureText("ACTIVIDAD")
            var maxActWordW = 0f
            for (item in entries) {
                val words = item.actividad.split(" ", "/", "-").map { it.trim() }.filter { it.isNotBlank() }
                for (w in words) {
                    val wordLen = if (w.length > 12) 12 else w.length
                    val ww = actTextPaint.measureText(w.take(wordLen))
                    if (ww > maxActWordW) maxActWordW = ww
                }
            }
            val col3W = maxOf(actHeaderW + 12f, maxActWordW + 10f).coerceAtLeast(75f)

            // 4: LUGAR (Wraps long words, fits longest token without pin)
            val lugarHeaderW = tableHeaderPaint.measureText("LUGAR")
            var maxLugarWordW = 0f
            for (item in entries) {
                val clean = item.lugar.removePrefix("📍").trim()
                val words = clean.split(" ", "/", "-").map { it.trim() }.filter { it.isNotBlank() }
                for (w in words) {
                    val wordLen = if (w.length > 12) 12 else w.length
                    val ww = lugarTextPaint.measureText(w.take(wordLen))
                    if (ww > maxLugarWordW) maxLugarWordW = ww
                }
            }
            val col4W = maxOf(lugarHeaderW + 12f, maxLugarWordW + 10f).coerceAtLeast(65f)

            // 5: COORDI (Stacked 1 below the other, only needs to fit 1 coordinator chip)
            val coordiHeaderW = tableHeaderPaint.measureText("COORDI")
            var maxCoordiChipW = 0f
            for (item in entries) {
                val opsList = item.responsables.split(",").map { it.trim() }.filter { it.isNotBlank() }
                for (op in opsList) {
                    val chipW = actTextPaint.measureText(op) + chipPadX * 2
                    if (chipW > maxCoordiChipW) maxCoordiChipW = chipW
                }
            }
            val col5W = maxOf(coordiHeaderW + 12f, maxCoordiChipW + 8f).coerceAtLeast(65f)

            // 6: OBSERVACIONES (Strictly limited to the title word "OBSERVACIONES")
            val obsTitleWidth = tableHeaderPaint.measureText("OBSERVACIONES")
            val col6W = obsTitleWidth + 10f

            val colWidths = floatArrayOf(
                col0W,
                col1W,
                col2W,
                col3W,
                col4W,
                col5W,
                col6W
            )

            val totalTableWidth = colWidths.sum()
            val imgWidth = (totalTableWidth + marginLeft + marginRight).toInt()

            // Header and banner heights
            val topHeaderHeight = 108f
            val subHeaderHeight = 36f
            val tableHeaderHeight = 44f
            val footerHeight = 48f
            val headerGreenColor = AndroidColor.parseColor("#2E7D32")

            // Calculate height needed for each row
            val chipLineHeight = 26f
            val chipGapY = 4f
            val rowHeights = FloatArray(entries.size)

            for (i in entries.indices) {
                val item = entries[i]
                var minH = 46f

                // 1. Chicos chip lines (MAX 2 CHICOS PER LINE)
                val chicosList = item.chicos.split(",").map { it.trim() }.filter { it.isNotBlank() }
                val chicosLineCount = if (chicosList.isEmpty()) 1 else ((chicosList.size + 1) / 2)
                val chicosH = chicosLineCount * chipLineHeight + (chicosLineCount - 1) * chipGapY + 14f
                if (chicosH > minH) minH = chicosH

                // 2. Operadores chip lines (ALWAYS 1 OPERADOR PER LINE, ONE BELOW THE OTHER)
                val opsList = item.responsables.split(",").map { it.trim() }.filter { it.isNotBlank() }
                val opsLineCount = if (opsList.isEmpty()) 1 else opsList.size
                val opsH = opsLineCount * chipLineHeight + (opsLineCount - 1) * chipGapY + 14f
                if (opsH > minH) minH = opsH

                // 3. Actividad double-line height
                val formattedAct = formatDoubleLineIfLong(item.actividad, 12)
                val actLayout = StaticLayout.Builder.obtain(
                    formattedAct,
                    0,
                    formattedAct.length,
                    actTextPaint,
                    (colWidths[3] - 6f).toInt().coerceAtLeast(10)
                ).build()
                val actH = actLayout.height + 14f
                if (actH > minH) minH = actH

                // 4. Lugar double-line height (without pin)
                val cleanLugar = item.lugar.removePrefix("📍").trim()
                val formattedLugar = formatDoubleLineIfLong(cleanLugar, 12)
                val lugarLayout = StaticLayout.Builder.obtain(
                    formattedLugar,
                    0,
                    formattedLugar.length,
                    lugarTextPaint,
                    (colWidths[4] - 6f).toInt().coerceAtLeast(10)
                ).build()
                val lugarH = lugarLayout.height + 14f
                if (lugarH > minH) minH = lugarH

                // 5. Measure observations text with strict width (adds line if exceeds)
                if (item.observaciones.isNotBlank()) {
                    val obsLayout = StaticLayout.Builder.obtain(
                        item.observaciones,
                        0,
                        item.observaciones.length,
                        obsTextPaint,
                        (col6W - 6f).toInt().coerceAtLeast(10)
                    ).build()
                    val obsH = obsLayout.height + 14f
                    if (obsH > minH) minH = obsH
                }

                rowHeights[i] = minH
            }

            val totalRowsHeight = rowHeights.sum()
            val totalHeight = (topHeaderHeight + subHeaderHeight + tableHeaderHeight + totalRowsHeight + footerHeight).toInt()

            // Supersampling scale factor for pristine high-definition output (2.5x resolution)
            // Ensures text remains crisp and pin-sharp when zoomed or sent via WhatsApp
            val scaleFactor = 2.5f
            val outWidth = (imgWidth * scaleFactor).toInt()
            val outHeight = (totalHeight * scaleFactor).toInt()

            val bitmap = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.scale(scaleFactor, scaleFactor)

            // Canvas background
            canvas.drawColor(AndroidColor.parseColor("#F8FAFC"))

            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG)

            // 1. Top Title Banner - Clean White Background
            paint.color = AndroidColor.WHITE
            canvas.drawRect(0f, 0f, imgWidth.toFloat(), topHeaderHeight, paint)

            // Top accent bar in the same green tone as column headers
            paint.color = headerGreenColor
            canvas.drawRect(0f, 0f, imgWidth.toFloat(), 5f, paint)

            // High-definition Logo on the left (image fully respected 1:1 with celeste frame)
            val logoSize = 78f
            val logoRect = RectF(marginLeft + 2f, 15f, marginLeft + 2f + logoSize, 15f + logoSize)
            val logoOptions = BitmapFactory.Options().apply {
                inScaled = false
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val logoBitmap = try {
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_logo_hogar, logoOptions)
                    ?: BitmapFactory.decodeStream(context.resources.openRawResource(R.drawable.ic_logo_hogar), null, logoOptions)
            } catch (_: Exception) {
                null
            }

            if (logoBitmap != null) {
                val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG).apply {
                    isFilterBitmap = true
                }
                // Render the complete 1:1 logo artwork perfectly without rounded clipping
                canvas.drawBitmap(logoBitmap, null, logoRect, logoPaint)

                // Marco en color celeste similar al tono celeste del logo (#38BDF8)
                paint.color = AndroidColor.parseColor("#38BDF8")
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2.5f
                canvas.drawRect(logoRect, paint)
                paint.style = Paint.Style.FILL
            } else {
                paint.color = AndroidColor.parseColor("#FFFBEB")
                canvas.drawRect(logoRect, paint)
                paint.color = headerGreenColor
                paint.textSize = 34f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("🏡", logoRect.centerX(), logoRect.centerY() + 12f, paint)
            }

            // Main Title: Enlarged with 3D extruded format
            val titleX = logoRect.right + 14f
            val titleY = 50f
            val titleText = "AGENDA HOGARES"

            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                textSize = 33f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.LEFT
            }

            // 3D Layer 2: Deep celeste shadow matching logo frame theme
            titlePaint.color = AndroidColor.parseColor("#0284C7")
            canvas.drawText(titleText, titleX + 2.5f, titleY + 2.5f, titlePaint)

            // 3D Layer 1: Celeste shadow matching the exact logo frame color (#38BDF8)
            titlePaint.color = AndroidColor.parseColor("#38BDF8")
            canvas.drawText(titleText, titleX + 1.2f, titleY + 1.2f, titlePaint)

            // 3D Front Face
            titlePaint.color = headerGreenColor
            canvas.drawText(titleText, titleX, titleY, titlePaint)

            // Date directly under title on the left
            paint.color = AndroidColor.parseColor("#334155")
            paint.textSize = 15f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("📅  $dateKey", titleX, 78f, paint)

            // 2. Sub-Header info bar
            var currentY = topHeaderHeight
            paint.color = AndroidColor.parseColor("#F1F5F9")
            canvas.drawRect(marginLeft, currentY, imgWidth - marginRight, currentY + subHeaderHeight, paint)

            paint.color = AndroidColor.parseColor("#334155")
            paint.textSize = 15f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("${entries.size} Actividades", marginLeft + 12f, currentY + 24f, paint)

            // Sync indicator
            paint.color = AndroidColor.parseColor("#10B981")
            canvas.drawCircle(imgWidth - marginRight - 110f, currentY + 19f, 5f, paint)
            paint.color = AndroidColor.parseColor("#047857")
            paint.textSize = 14f
            canvas.drawText("Sincronizado", imgWidth - marginRight - 98f, currentY + 24f, paint)

            currentY += subHeaderHeight

            // 3. Table Header
            paint.color = headerGreenColor
            canvas.drawRect(marginLeft, currentY, imgWidth - marginRight, currentY + tableHeaderHeight, paint)

            val headers = arrayOf(
                "#",
                "HORA ▲",
                "NIÑA/O",
                "ACTIVIDAD",
                "LUGAR",
                "COORDI",
                "OBSERVACIONES"
            )

            val headerAlignments = arrayOf(
                Paint.Align.CENTER,
                Paint.Align.CENTER,
                Paint.Align.CENTER,
                Paint.Align.CENTER,
                Paint.Align.CENTER,
                Paint.Align.CENTER,
                Paint.Align.CENTER
            )

            var headX = marginLeft
            val headerGridPaint = Paint().apply {
                color = AndroidColor.parseColor("#1B5E20")
                strokeWidth = 1.5f
            }

            for (h in headers.indices) {
                val colW = colWidths[h]
                val align = headerAlignments[h]
                tableHeaderPaint.textAlign = align

                val textX = when (align) {
                    Paint.Align.CENTER -> headX + colW / 2f
                    Paint.Align.LEFT -> headX + 6f
                    else -> headX + colW - 6f
                }
                canvas.drawText(headers[h], textX, currentY + 29f, tableHeaderPaint)

                headX += colW
                if (h < headers.size - 1) {
                    canvas.drawLine(headX, currentY, headX, currentY + tableHeaderHeight, headerGridPaint)
                }
            }

            currentY += tableHeaderHeight

            // 4. Data Rows
            val gridLinePaint = Paint().apply {
                color = AndroidColor.parseColor("#94A3B8")
                strokeWidth = 1.2f
            }

            for (r in entries.indices) {
                val item = entries[r]
                val rHeight = rowHeights[r]
                val rowBg = if (r % 2 == 0) AndroidColor.WHITE else AndroidColor.parseColor("#E2E8F0")

                paint.color = rowBg
                canvas.drawRect(marginLeft, currentY, imgWidth - marginRight, currentY + rHeight, paint)

                var xPos = marginLeft

                // Col 0: # Index
                val col0W = colWidths[0]
                paint.color = AndroidColor.parseColor("#64748B")
                paint.textSize = 15f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("${r + 1}", xPos + col0W / 2f, currentY + rHeight / 2f + 5f, paint)
                xPos += col0W
                canvas.drawLine(xPos, currentY, xPos, currentY + rHeight, gridLinePaint)

                // Col 1: HORA
                val col1W = colWidths[1]
                val timePillX = xPos + (col1W - timePillW) / 2f
                val timePillH = 24f
                val timePillY = currentY + (rHeight - timePillH) / 2f

                paint.color = AndroidColor.parseColor("#E0F2FE")
                canvas.drawRoundRect(RectF(timePillX, timePillY, timePillX + timePillW, timePillY + timePillH), 5f, 5f, paint)
                paint.color = AndroidColor.parseColor("#0284C7")
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1.2f
                canvas.drawRoundRect(RectF(timePillX, timePillY, timePillX + timePillW, timePillY + timePillH), 5f, 5f, paint)
                paint.style = Paint.Style.FILL

                paint.color = AndroidColor.parseColor("#0369A1")
                paint.textSize = 13f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText(item.time, timePillX + timePillW / 2f, timePillY + 16.5f, paint)
                xPos += col1W
                canvas.drawLine(xPos, currentY, xPos, currentY + rHeight, gridLinePaint)

                // Col 2: NIÑA / NIÑO (MAX 2 CHICOS PER LINE, TIGHT 1-CHAR PADDING RECTANGLES!)
                val col2W = colWidths[2]
                val chicosList = item.chicos.split(",").map { it.trim() }.filter { it.isNotBlank() }
                val chicosLineCount = if (chicosList.isEmpty()) 1 else ((chicosList.size + 1) / 2)
                val chicosContentH = chicosLineCount * chipLineHeight + (chicosLineCount - 1) * chipGapY
                val chicosStartY = currentY + ((rHeight - chicosContentH) / 2f).coerceAtLeast(6f)

                canvas.save()
                canvas.clipRect(xPos + 1f, currentY + 1f, xPos + col2W - 1f, currentY + rHeight - 1f)
                drawTwoPerLineChips(
                    canvas = canvas,
                    items = chicosList,
                    startX = xPos + 4f,
                    startY = chicosStartY,
                    colWidth = col2W - 8f,
                    chipLineHeight = chipLineHeight,
                    chipGapY = chipGapY,
                    colorProvider = { name ->
                        chicosColorMap[name.uppercase()] ?: Pair(AndroidColor.parseColor("#3B82F6"), AndroidColor.WHITE)
                    }
                )
                canvas.restore()

                xPos += col2W
                canvas.drawLine(xPos, currentY, xPos, currentY + rHeight, gridLinePaint)

                // Col 3: ACTIVIDAD (Doble renglón si palabras > 12 chars, BLACK TEXT, CLIPPED TO CELL)
                val col3W = colWidths[3]
                val formattedAct = formatDoubleLineIfLong(item.actividad, 12)
                val actLayout = StaticLayout.Builder.obtain(
                    formattedAct,
                    0,
                    formattedAct.length,
                    actTextPaint,
                    (col3W - 6f).toInt().coerceAtLeast(10)
                ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

                canvas.save()
                canvas.clipRect(xPos + 1f, currentY + 1f, xPos + col3W - 1f, currentY + rHeight - 1f)
                val actY = currentY + (rHeight - actLayout.height) / 2f
                canvas.translate(xPos + 3f, actY.coerceAtLeast(currentY + 4f))
                actLayout.draw(canvas)
                canvas.restore()

                xPos += col3W
                canvas.drawLine(xPos, currentY, xPos, currentY + rHeight, gridLinePaint)

                // Col 4: LUGAR (Doble renglón si palabras > 12 chars, NO PIN, BLACK TEXT, CLIPPED TO CELL)
                val col4W = colWidths[4]
                val cleanLugar = item.lugar.removePrefix("📍").trim()
                val formattedLugar = formatDoubleLineIfLong(cleanLugar, 12)
                val lugarLayout = StaticLayout.Builder.obtain(
                    formattedLugar,
                    0,
                    formattedLugar.length,
                    lugarTextPaint,
                    (col4W - 6f).toInt().coerceAtLeast(10)
                ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

                canvas.save()
                canvas.clipRect(xPos + 1f, currentY + 1f, xPos + col4W - 1f, currentY + rHeight - 1f)
                val lugarY = currentY + (rHeight - lugarLayout.height) / 2f
                canvas.translate(xPos + 3f, lugarY.coerceAtLeast(currentY + 4f))
                lugarLayout.draw(canvas)
                canvas.restore()

                xPos += col4W
                canvas.drawLine(xPos, currentY, xPos, currentY + rHeight, gridLinePaint)

                // Col 5: COORDI (STACKED ONE BELOW THE OTHER, TIGHT 1-CHAR PADDING RECTANGLES!)
                val col5W = colWidths[5]
                val opsList = item.responsables.split(",").map { it.trim() }.filter { it.isNotBlank() }
                val opsContentH = if (opsList.isEmpty()) chipLineHeight else (opsList.size * chipLineHeight + (opsList.size - 1) * chipGapY)
                val opsStartY = currentY + ((rHeight - opsContentH) / 2f).coerceAtLeast(5f)

                canvas.save()
                canvas.clipRect(xPos + 1f, currentY + 1f, xPos + col5W - 1f, currentY + rHeight - 1f)
                drawVerticalStackedChips(
                    canvas = canvas,
                    items = opsList,
                    startX = xPos + 3f,
                    startY = opsStartY,
                    colWidth = col5W - 6f,
                    chipLineHeight = chipLineHeight,
                    chipGapY = chipGapY,
                    colorProvider = { name ->
                        operadoresColorMap[name.uppercase()] ?: Pair(AndroidColor.parseColor("#2563EB"), AndroidColor.WHITE)
                    }
                )
                canvas.restore()

                xPos += col5W
                canvas.drawLine(xPos, currentY, xPos, currentY + rHeight, gridLinePaint)

                // Col 6: OBSERVACIONES (Width limited to "OBSERVACIONES" title, BLACK TEXT, CLIPPED)
                val col6Width = colWidths[6]
                canvas.save()
                canvas.clipRect(xPos + 1f, currentY + 1f, xPos + col6Width - 1f, currentY + rHeight - 1f)
                if (item.observaciones.isNotBlank()) {
                    val obsLayout = StaticLayout.Builder.obtain(
                        item.observaciones,
                        0,
                        item.observaciones.length,
                        obsTextPaint,
                        (col6Width - 6f).toInt().coerceAtLeast(10)
                    ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

                    val obsTopOffset = (rHeight - obsLayout.height) / 2f
                    canvas.translate(xPos + 3f, currentY + obsTopOffset.coerceAtLeast(5f))
                    obsLayout.draw(canvas)
                } else {
                    paint.color = AndroidColor.parseColor("#94A3B8")
                    paint.textSize = 14f
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText("-", xPos + col6Width / 2f, currentY + rHeight / 2f + 5f, paint)
                }
                canvas.restore()

                // Horizontal row divider line
                canvas.drawLine(marginLeft, currentY + rHeight, imgWidth - marginRight, currentY + rHeight, gridLinePaint)

                currentY += rHeight
            }

            // Outer table border
            paint.color = AndroidColor.parseColor("#64748B")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawRect(
                marginLeft,
                topHeaderHeight + subHeaderHeight,
                imgWidth - marginRight,
                currentY,
                paint
            )
            paint.style = Paint.Style.FILL

            // 5. Footer
            val footerY = currentY + 10f
            paint.color = AndroidColor.parseColor("#64748B")
            paint.textSize = 13.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("AGENDA HOGARES", imgWidth / 2f, footerY + 18f, paint)

            // Save to Cache File
            val imagesDir = File(context.cacheDir, "images")
            if (!imagesDir.exists()) imagesDir.mkdirs()

            val cleanDate = dateKey.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val outputFile = File(imagesDir, "agenda_hogares_$cleanDate.jpg")

            val fos = FileOutputStream(outputFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()

            return outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Strictly draws at most 2 chips per row in the given column width.
     * Snug rectangle: strictly 1 character distance between the letter and the edge.
     * If there are >2 items, it expands downward to the next line (renglón).
     */
    private fun drawTwoPerLineChips(
        canvas: Canvas,
        items: List<String>,
        startX: Float,
        startY: Float,
        colWidth: Float,
        chipLineHeight: Float,
        chipGapY: Float,
        colorProvider: (String) -> Pair<Int, Int>
    ) {
        if (items.isEmpty()) return

        val chipTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 13.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val chipBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Strictly 1 character distance between letter and limit of rectangle
        val charWidth = chipTextPaint.measureText("N") * 0.75f
        val chipPaddingX = charWidth.coerceIn(5f, 7f)
        val gapX = 4f

        var currentY = startY

        // Group into chunks of maximum 2 items per line!
        items.chunked(2).forEach { rowItems ->
            val snugWidths = rowItems.map { name ->
                val textW = chipTextPaint.measureText(name)
                textW + chipPaddingX * 2f
            }

            val totalSnugW = snugWidths.sum() + (rowItems.size - 1) * gapX
            val scale = if (totalSnugW > colWidth) (colWidth - (rowItems.size - 1) * gapX) / snugWidths.sum() else 1f

            var currentX = startX + (if (rowItems.size == 1) (colWidth - (snugWidths[0] * scale)) / 2f else 0f)

            rowItems.forEachIndexed { idx, name ->
                val (bgColor, _) = colorProvider(name)
                chipTextPaint.color = AndroidColor.WHITE
                chipBgPaint.color = bgColor

                val thisChipW = snugWidths[idx] * scale
                val chipRect = RectF(currentX, currentY, currentX + thisChipW, currentY + chipLineHeight)
                canvas.drawRoundRect(chipRect, 5f, 5f, chipBgPaint)

                canvas.drawText(
                    name,
                    chipRect.centerX(),
                    chipRect.centerY() + 4.5f,
                    chipTextPaint
                )

                currentX += thisChipW + gapX
            }

            currentY += chipLineHeight + chipGapY
        }
    }

    /**
     * Strictly draws operators vertically, one below the other in the same cell.
     * Snug rectangle: strictly 1 character distance between the letter and the edge.
     */
    private fun drawVerticalStackedChips(
        canvas: Canvas,
        items: List<String>,
        startX: Float,
        startY: Float,
        colWidth: Float,
        chipLineHeight: Float,
        chipGapY: Float,
        colorProvider: (String) -> Pair<Int, Int>
    ) {
        if (items.isEmpty()) return

        val chipTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 13.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val chipBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Strictly 1 character distance between letter and limit of rectangle
        val charWidth = chipTextPaint.measureText("N") * 0.75f
        val chipPaddingX = charWidth.coerceIn(5f, 7f)

        var currentY = startY

        for (name in items) {
            val (bgColor, textColor) = colorProvider(name)
            chipTextPaint.color = textColor
            chipBgPaint.color = bgColor

            val textW = chipTextPaint.measureText(name)
            val snugW = (textW + chipPaddingX * 2f).coerceAtMost(colWidth)
            val chipX = startX + (colWidth - snugW) / 2f

            val chipRect = RectF(chipX, currentY, chipX + snugW, currentY + chipLineHeight)
            canvas.drawRoundRect(chipRect, 5f, 5f, chipBgPaint)

            canvas.drawText(
                name,
                chipRect.centerX(),
                chipRect.centerY() + 4.5f,
                chipTextPaint
            )

            currentY += chipLineHeight + chipGapY
        }
    }

    /**
     * Saves the JPG file directly to the device's public Pictures / Downloads gallery
     * so it appears instantly in Google Photos, Gallery, and Files app.
     */
    fun saveJpgToDevice(context: Context, jpgFile: File, dateKey: String): Boolean {
        return try {
            val cleanDate = dateKey.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val fileName = "Agenda_Hogares_${cleanDate}_${System.currentTimeMillis()}.jpg"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AgendaHogares")
                }

                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(jpgFile).use { input ->
                            input.copyTo(out)
                        }
                    }
                    Toast.makeText(context, "Imagen guardada en Galería (Fotos / AgendaHogares)", Toast.LENGTH_LONG).show()
                    true
                } else {
                    false
                }
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(picturesDir, "AgendaHogares")
                if (!appDir.exists()) appDir.mkdirs()

                val destFile = File(appDir, fileName)
                FileInputStream(jpgFile).use { input ->
                    FileOutputStream(destFile).use { out ->
                        input.copyTo(out)
                    }
                }

                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(destFile.absolutePath),
                    arrayOf("image/jpeg")
                ) { _, _ -> }

                Toast.makeText(context, "Imagen guardada en Galería", Toast.LENGTH_LONG).show()
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al guardar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    /**
     * Shares the JPG image via Android Sharesheet / WhatsApp
     */
    fun shareJpgToWhatsApp(context: Context, file: File, caption: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, caption)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val chooserIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, caption)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(chooserIntent, "Compartir planilla vía...")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }

    fun shareJpgImage(context: Context, file: File, dateKey: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val text = "AGENDA HOGARES\n$dateKey"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "AGENDA HOGARES - $dateKey")
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Compartir planilla JPG vía...")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
