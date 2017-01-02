package view;

import javafx.beans.property.BooleanProperty;

/**
 * A ui element that can be selected by the user
 */
public interface Selectable {
    boolean isSelected();

    void setSelected(boolean selected);

    BooleanProperty isSelectedProperty();
}
