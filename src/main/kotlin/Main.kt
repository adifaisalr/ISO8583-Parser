@file:OptIn(ExperimentalStdlibApi::class)

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }
    var textInput by remember { mutableStateOf("Hello, World!") }

    MaterialTheme {
        Column(modifier = Modifier.padding(10.dp)) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = textInput,
                onValueChange = {
                    textInput = it
                }
            )
            Button(onClick = {
                text = parseISO(textInput)
            }) {
                Text("Parse ISO8583")
            }
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = text,
                onValueChange = { },
                enabled = false,
            )
        }
    }
}

private fun parseISO(msg: String): String {
    var result = ""
    var curIndex = 0
    val fieldMap = IsoPackager.getIsoFieldMap()
    var fieldMapList: List<Char> = listOf()
    if (msg.length < 20) return "Invalid ISO format"

    fieldMap.forEach {
        when (it.id) {
            0 -> {
                val msgTypeIndicator = msg.substring(curIndex, it.length)
                curIndex += it.length
                result += "${it.name} : $msgTypeIndicator \n"
            }

            1 -> {
                val bitMap = msg.substring(curIndex, curIndex + it.length)
                curIndex += it.length
                result += "${it.name} : $bitMap \n"
                val binaryStr = hexToBinary(bitMap.substring(0, 8)) + hexToBinary(bitMap.substring(8, 16))
                fieldMapList = binaryStr.toList()
                result += "binary : $binaryStr \n"
                result += "binary length : ${binaryStr.length} \n"
            }

            else -> {
                if (it.id - 1 >= fieldMapList.size || fieldMapList[it.id - 1] == '0') return@forEach
                var len = it.length
                val digitLen = len.toString().length
                if (!it.fixed) {
                    len = msg.substring(curIndex, curIndex + digitLen).toIntOrNull() ?: return "Invalid ISO format"
                    curIndex += digitLen
                }
                when (it.type) {
                    FieldType.ALPHANUMERIC, FieldType.ALPHANUMERIC_S -> len *= 2
                    else -> {}
                }
                var until = curIndex + len
                if (until >= msg.length) until = msg.length
                val fieldVal = msg.substring(curIndex, until)
                result += "${it.name} : $fieldVal \n"
                curIndex = until
            }
        }
    }
    return result
}

fun hexToBinary(hex: String): String {
    val i = hex.hexToInt()
    val len = hex.length * 4
    var bin = Integer.toBinaryString(i)
    bin = bin.padStart(len, '0')
    return bin
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
