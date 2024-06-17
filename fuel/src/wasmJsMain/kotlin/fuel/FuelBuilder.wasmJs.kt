package fuel

public actual class FuelBuilder actual constructor() {
    public actual fun build(): HttpLoader = WasmHttpLoader()
}
