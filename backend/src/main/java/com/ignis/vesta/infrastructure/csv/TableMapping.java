package com.ignis.vesta.infrastructure.csv;

import com.ignis.vesta.domain.normalization.Quantity;
import com.ignis.vesta.domain.normalization.SourceUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Curated column mapping for one PSI experimental-table file. Headers are written exactly as documented by PSI
 * (matching ignores case and repeated spaces). When several columns map to one quantity (e.g. initial and final
 * O2), the stored value is the range spanning them.
 */
record TableMapping(String fileName, String labelColumn, String fuelColumn, List<QuantityColumn> quantities) {

    record QuantityColumn(String header, SourceUnit unit) {
        Quantity quantity() {
            return unit.quantity();
        }
    }

    TableMapping {
        Objects.requireNonNull(fileName);
        quantities = List.copyOf(quantities);
    }

    static Builder file(String fileName) {
        return new Builder(fileName);
    }

    static final class Builder {
        private final String fileName;
        private String label;
        private String fuel;
        private final List<QuantityColumn> quantities = new ArrayList<>();

        private Builder(String fileName) {
            this.fileName = fileName;
        }

        Builder label(String header) {
            this.label = header;
            return this;
        }

        Builder fuel(String header) {
            this.fuel = header;
            return this;
        }

        Builder o2(String header, SourceUnit unit) {
            return quantity(header, unit, Quantity.O2_FRACTION);
        }

        Builder pressure(String header, SourceUnit unit) {
            return quantity(header, unit, Quantity.PRESSURE_ATM);
        }

        Builder flow(String header) {
            return quantity(header, SourceUnit.CM_PER_S, Quantity.FLOW_CM_PER_S);
        }

        private Builder quantity(String header, SourceUnit unit, Quantity expected) {
            if (unit.quantity() != expected) {
                throw new IllegalArgumentException(unit + " is not a unit of " + expected);
            }
            quantities.add(new QuantityColumn(header, unit));
            return this;
        }

        TableMapping build() {
            return new TableMapping(fileName, label, fuel, quantities);
        }
    }
}
