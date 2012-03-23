/**
 * Copyright (c) 2011 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * Portions Copyrighted 2011 [name of copyright owner]
 */
package com.evolveum.midpoint.prism.delta;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.Itemable;
import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObjectDefinition;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.PropertyPath;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.Dumpable;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * @author Radovan Semancik
 * 
 */
public abstract class ItemDelta<V extends PrismValue> implements Itemable, Dumpable, DebugDumpable {

	/**
	 * Name of the property
	 */
	protected QName name;
	/**
	 * Parent path of the property (path to the property container)
	 */
	protected PropertyPath parentPath;
	protected ItemDefinition definition;
	protected PrismContext prismContext;

	protected Collection<V> valuesToReplace = null;
	protected Collection<V> valuesToAdd = null;
	protected Collection<V> valuesToDelete = null;

	public ItemDelta(ItemDefinition itemDefinition) {
		this.name = itemDefinition.getName();
		this.parentPath = new PropertyPath();
		this.definition = itemDefinition;
	}

	public ItemDelta(QName name, ItemDefinition itemDefinition) {
		this.name = name;
		this.parentPath = new PropertyPath();
		this.definition = itemDefinition;
	}

	public ItemDelta(PropertyPath parentPath, QName name, ItemDefinition itemDefinition) {
		this.name = name;
		this.parentPath = parentPath;
		this.definition = itemDefinition;
	}

	public ItemDelta(PropertyPath propertyPath, ItemDefinition itemDefinition) {
		this.name = propertyPath.last().getName();
		this.parentPath = propertyPath.allExceptLast();
		this.definition = itemDefinition;
	}

	public QName getName() {
		return name;
	}

	public void setName(QName name) {
		this.name = name;
	}

	public PropertyPath getParentPath() {
		return parentPath;
	}

	public void setParentPath(PropertyPath parentPath) {
		this.parentPath = parentPath;
	}

	public PropertyPath getPath() {
		return getParentPath().subPath(name);
	}

	@Override
	public PropertyPath getPath(PropertyPath pathPrefix) {
		return pathPrefix.subPath(name);
	}

	public ItemDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(ItemDefinition definition) {
		this.definition = definition;
	}

	public void applyDefinition(ItemDefinition definition) throws SchemaException {
		this.definition = definition;
		if (getValuesToAdd() != null) {
			for (V pval : getValuesToAdd()) {
				pval.applyDefinition(definition);
			}
		}
		if (getValuesToDelete() != null) {
			for (V pval : getValuesToDelete()) {
				pval.applyDefinition(definition);
			}
		}
		if (getValuesToReplace() != null) {
			for (V pval : getValuesToReplace()) {
				pval.applyDefinition(definition);
			}
		}
	}

	public static void applyDefinition(Collection<? extends ItemDelta> deltas,
			PrismObjectDefinition definition) throws SchemaException {
		for (ItemDelta<?> itemDelta : deltas) {
			PropertyPath path = itemDelta.getPath();
			ItemDefinition itemDefinition = definition.findItemDefinition(path, ItemDefinition.class);
			itemDelta.applyDefinition(itemDefinition);
		}
	}

	public PrismContext getPrismContext() {
		return prismContext;
	}

	public void setPrismContext(PrismContext prismContext) {
		this.prismContext = prismContext;
	}

	public abstract Class<? extends Item> getItemClass();

	public Collection<V> getValuesToAdd() {
		return valuesToAdd;
	}

	public Collection<V> getValuesToDelete() {
		return valuesToDelete;
	}

	public Collection<V> getValuesToReplace() {
		return valuesToReplace;
	}

	public void addValuesToAdd(Collection<V> newValues) {
		for (V val: newValues) {
			addValueToAdd(val);
		}
	}

	public void addValueToAdd(V newValue) {
		if (valuesToAdd == null) {
			valuesToAdd = newValueCollection();
		}
		valuesToAdd.add(newValue);
		newValue.setParent(this);
	}

	public void addValuesToDelete(Collection<V> newValues) {
		for (V val: newValues) {
			addValueToDelete(val);
		}
	}

	public void addValueToDelete(V newValue) {
		if (valuesToDelete == null) {
			valuesToDelete = newValueCollection();
		}
		valuesToDelete.add(newValue);
		newValue.setParent(this);
	}

	public void setValuesToReplace(Collection<V> newValues) {
		if (valuesToReplace == null) {
			valuesToReplace = newValueCollection();
		} else {
			valuesToReplace.clear();
		}
		for (V val: newValues) {
			valuesToReplace.add(val);
			val.setParent(this);
		}
	}

	public void setValueToReplace(V newValue) {
		if (valuesToReplace == null) {
			valuesToReplace = newValueCollection();
		} else {
			valuesToReplace.clear();
		}
		valuesToReplace.add(newValue);
		newValue.setParent(this);
	}

	private Collection<V> newValueCollection() {
		return new HashSet<V>();
	}

	public boolean isValueToAdd(V value) {
		if (valuesToAdd == null) {
			return false;
		}
		return valuesToAdd.contains(value);
	}

	public boolean isValueToDelete(V value) {
		if (valuesToDelete == null) {
			return false;
		}
		return valuesToDelete.contains(value);
	}

	public boolean isEmpty() {
		if (valuesToAdd == null && valuesToDelete == null && valuesToReplace == null) {
			return true;
		}
		return false;
	}

	public boolean isReplace() {
		return (valuesToReplace != null);
	}

	public boolean isAdd() {
		return (valuesToAdd != null && !valuesToAdd.isEmpty());
	}

	public boolean isDelete() {
		return (valuesToDelete != null && !valuesToDelete.isEmpty());
	}

	public void clear() {
		valuesToReplace = null;
		valuesToAdd = null;
		valuesToDelete = null;
	}

	public void checkConsistence() {
		if (valuesToReplace != null && (valuesToAdd != null || valuesToDelete != null)) {
			throw new IllegalStateException(
					"The delta cannot be both 'replace' and 'add/delete' at the same time");
		}
	}
	
	/**
	 * Distributes the replace values of this delta to add and delete with respect to provided existing values.
	 */
	public void distributeReplace(Collection<V> existingValues) {
		if (existingValues != null) {
			for (V existingVal: existingValues) {
				if (!isIn(getValuesToReplace(), existingVal)) {
					addValueToDelete((V)existingVal.clone());
				}
			}
		}
		for (V replaceVal: getValuesToReplace()) {
			if (!isIn(existingValues,replaceVal) && !isIn(getValuesToAdd(), replaceVal)) {
				getValuesToAdd().add(replaceVal);
			}
		}
		getValuesToReplace().clear();
	}

	private boolean isIn(Collection<V> values, V val) {
		if (values == null) {
			return false;
		}
		for (V v: values) {
			if (v.equalsRealValue(val)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Merge specified delta to this delta. This delta is assumed to be
	 * chronologically earlier.
	 */
	public void merge(ItemDelta deltaToMerge) {
		checkConsistence();
		deltaToMerge.checkConsistence();
		if (deltaToMerge.isEmpty()) {
			return;
		}
		if (deltaToMerge.valuesToReplace != null) {
			if (this.valuesToReplace != null) {
				this.valuesToReplace.clear();
				this.valuesToReplace.addAll(deltaToMerge.valuesToReplace);
			}
			this.valuesToReplace = newValueCollection();
			this.valuesToReplace.addAll(deltaToMerge.valuesToReplace);
		} else {
			addValuesToAdd(deltaToMerge.valuesToAdd);
			addValuesToDelete(deltaToMerge.valuesToDelete);
		}
	}

	/**
	 * Apply this delta (path) to a property container.
	 */
	public void applyTo(PrismContainer<?> propertyContainer) throws SchemaException {
		Item<?> item = propertyContainer.findOrCreateItem(getPath(), getItemClass(), getDefinition());
		applyTo(item);
		if (item.isEmpty()) {
			propertyContainer.remove(item);
		}
	}

	public static void applyTo(Collection<? extends ItemDelta> deltas, PrismContainer propertyContainer) throws SchemaException {
		for (ItemDelta delta : deltas) {
			delta.applyTo(propertyContainer);
		}
	}

	/**
	 * Apply this delta (path) to a property.
	 */
	public void applyTo(Item item) throws SchemaException {
		if (valuesToReplace != null) {
			item.replaceAll(valuesToReplace);
			return;
		}
		if (valuesToAdd != null) {
			item.addAll(valuesToAdd);
		}
		if (valuesToDelete != null) {
			item.removeAll(valuesToDelete);
		}
	}

	/**
	 * Returns the "new" state of the property - the state that would be after
	 * the delta is applied. Assumes "replace" delta.
	 */
	public Item getItemNew() {
		if (valuesToAdd != null && valuesToDelete != null) {
			throw new IllegalStateException("Cannot fetch new item state, not a 'replace' delta");
		}
		Item item = definition.instantiate();
		if (valuesToReplace == null || valuesToReplace.isEmpty()) {
			return item;
		}
		item.getValues().addAll((Collection) valuesToReplace);
		return item;
	}
	
	public abstract ItemDelta clone();
	
	protected void copyValues(ItemDelta clone) {
		clone.definition = this.definition;
		clone.name = this.name;
		clone.parentPath = this.parentPath;
		clone.prismContext = this.prismContext;
		clone.valuesToAdd = cloneSet(this.valuesToAdd);
		clone.valuesToDelete = cloneSet(this.valuesToDelete);
		clone.valuesToReplace = cloneSet(this.valuesToReplace);
	}

	private Collection<V> cloneSet(Collection<V> thisSet) {
		if (thisSet == null) {
			return null;
		}
		Collection<V> clonedSet = newValueCollection();
		for (V thisVal: thisSet) {
			V clonedVal = (V)thisVal.clone();
			clonedVal.setParent(this);
			clonedSet.add(clonedVal);
		}
		return clonedSet;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append("(");
		sb.append(parentPath).append(" / ").append(DebugUtil.prettyPrint(name));
		if (valuesToReplace != null) {
			sb.append(", REPLACE");
		}

		if (valuesToAdd != null) {
			sb.append(", ADD");
		}

		if (valuesToDelete != null) {
			sb.append(", DELETE");
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public String debugDump() {
		return debugDump(0);
	}

	@Override
	public String debugDump(int indent) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indent; i++) {
			sb.append(INDENT_STRING);
		}
		sb.append(getClass().getSimpleName()).append("(");
		sb.append(parentPath).append(" / ").append(DebugUtil.prettyPrint(name)).append(")");

		if (valuesToReplace != null) {
			sb.append("\n");
			dumpValues(sb, "REPLACE", valuesToReplace, indent + 1);
		}

		if (valuesToAdd != null) {
			sb.append("\n");
			dumpValues(sb, "ADD", valuesToAdd, indent + 1);
		}

		if (valuesToDelete != null) {
			sb.append("\n");
			dumpValues(sb, "DELETE", valuesToDelete, indent + 1);
		}

		return sb.toString();

	}

	public String dump() {
		return debugDump();
	}

	protected void dumpValues(StringBuilder sb, String label, Collection<V> values, int indent) {
		for (int i = 0; i < indent; i++) {
			sb.append(INDENT_STRING);
		}
		sb.append(label).append(": ");
		if (values == null) {
			sb.append("(null)");
		} else {
			Iterator<V> i = values.iterator();
			while (i.hasNext()) {
				sb.append(i.next());
				if (i.hasNext()) {
					sb.append(", ");
				}
			}
		}
	}

}
