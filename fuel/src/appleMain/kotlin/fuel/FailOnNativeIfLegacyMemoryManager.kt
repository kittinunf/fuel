package fuel

@OptIn(ExperimentalStdlibApi::class)
public fun failOnNativeIfLegacyMemoryManager() {
    check(isExperimentalMM()) {
        "Fuel: The legacy memory manager is no longer supported, please use the new memory manager instead. See https://github.com/JetBrains/kotlin/blob/master/kotlin-native/NEW_MM.md for more information."
    }
}
