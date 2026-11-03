package com.juanvargas.kubesynapse.domain.pattern.chain;

/**
 * Base Handler in the Chain of Responsibility pattern.
 */
public abstract class DiagnosticHandler {
    private DiagnosticHandler next;

    public DiagnosticHandler setNext(DiagnosticHandler next) {
        this.next = next;
        return next;
    }

    public void handle(DiagnosticContext context) {
        doHandle(context);

        if (!context.isConclusive() && next != null) {
            next.handle(context);
        }
    }

    protected abstract void doHandle(DiagnosticContext context);
}
