/*
 * Copyright (c) 2011-2014 Alliance for Permanent Access (APA) and its
 * contributors. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * The APA licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.scidipes.toolkits.pawebapp.model;

/**
 * @author Tom Bunting
 * 
 */
public final class FormCountByItemName {

	private final String displayName;

	private final long formCount;

	private final String group;

	private final int groupOrder;

	private final String name;

	public FormCountByItemName(final int groupOrder, final String group, final String name, final String displayName,
			final long formCount) {
		this.groupOrder = groupOrder;
		this.group = group;
		this.name = name;
		this.displayName = displayName;
		this.formCount = formCount;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof FormCountByItemName)) {
			return false;
		}
		final FormCountByItemName other = (FormCountByItemName) o;
		return name.equals(other.getName());
	}

	/**
	 * @return the displayName
	 */
	public final String getDisplayName() {
		return displayName;
	}

	/**
	 * @return the formCount
	 */
	public final long getFormCount() {
		return formCount;
	}

	/**
	 * @return the group
	 */
	public final String getGroup() {
		return group;
	}

	/**
	 * @return the groupOrder
	 */
	public final int getGroupOrder() {
		return groupOrder;
	}

	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

}
