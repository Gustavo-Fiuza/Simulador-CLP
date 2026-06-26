package screens.scenes;

public enum ScenesEnum {
    DEFAULT("Painel (padrão)"),
    BATCH_SIMULATION("Simulação Batch"),
    // GARAGE_SIMULATION("Simulação Portão Garagem"),
    ELEVATOR_SIMULATION("Simulação Elevador");

    private final String label;

    ScenesEnum(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
