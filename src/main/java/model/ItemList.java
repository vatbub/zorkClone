package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A list of {@link Item}s
 */
public class ItemList extends ArrayList<Item> implements Serializable {
    private transient List<ChangeListener> changeListenerList;

    public ItemList() {
        super();
    }

    @SuppressWarnings({"unused"})
    public ItemList(Collection<? extends Item> c) {
        super(c);
    }

    public List<ChangeListener> getChangeListenerList() {
        if (changeListenerList == null) {
            changeListenerList = new ArrayList<>();
        }
        return changeListenerList;
    }

    @Override
    public boolean remove(Object o) {
        for (ChangeListener changeListener : this.getChangeListenerList()) {
            changeListener.removed((Item) o);
        }
        return super.remove(o);
    }

    @Override
    public Item set(int index, Item element) {
        for (ChangeListener changeListener : this.getChangeListenerList()) {
            changeListener.replaced(index, this.get(index), element);
        }
        return super.set(index, element);
    }

    @Override
    public void add(int index, Item element) {
        for (ChangeListener changeListener : this.getChangeListenerList()) {
            changeListener.added(index, element);
        }
        super.add(index, element);
    }

    @Override
    public Item remove(int index) {
        for (ChangeListener changeListener : this.getChangeListenerList()) {
            changeListener.removed(this.get(index));
        }
        return super.remove(index);
    }

    public interface ChangeListener {
        void removed(Item item);

        void added(int index, Item item);

        void replaced(int index, Item oldValue, Item newValue);
    }
}
