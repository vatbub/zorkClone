package model;

/*-
 * #%L
 * Zork Clone
 * %%
 * Copyright (C) 2016 - 2017 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A list of {@link Entity}s
 */
public class EntityList extends ArrayList<Entity> implements Serializable {
    private transient List<ChangeListener> changeListenerList;

    public EntityList() {
        super();
    }

    @SuppressWarnings({"unused"})
    public EntityList(Collection<? extends Entity> c) {
        super(c);
    }

    public List<ChangeListener> getChangeListenerList() {
        if (changeListenerList == null) {
            changeListenerList = new ArrayList<>();
        }
        return changeListenerList;
    }

    @Override
    public Entity set(int index, Entity element) {
        for (ChangeListener changeListener : this.getChangeListenerList()) {
            changeListener.replaced(index, this.get(index), element);
        }
        return super.set(index, element);
    }

    @Override
    public void add(int index, Entity element) {
        for (ChangeListener changeListener : this.getChangeListenerList()) {
            changeListener.added(index, element);
        }
        super.add(index, element);
    }

    @Override
    public Entity remove(int index) {
        for (ChangeListener changeListener : this.getChangeListenerList()) {
            changeListener.removed(this.get(index));
        }
        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        for (ChangeListener changeListener : this.getChangeListenerList()) {
            changeListener.removed((Entity) o);
        }
        return super.remove(o);
    }

    @SuppressWarnings("unused")
    public interface ChangeListener {
        void removed(Entity item);

        void added(int index, Entity item);

        void replaced(int index, Entity oldValue, Entity newValue);
    }
}
