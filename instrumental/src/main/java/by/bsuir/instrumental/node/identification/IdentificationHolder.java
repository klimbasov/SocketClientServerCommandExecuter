package by.bsuir.instrumental.node.identification;

public interface IdentificationHolder extends Comparable<IdentificationHolder> {
    String getIdentifier();

    boolean isIdentificationValid();
}
