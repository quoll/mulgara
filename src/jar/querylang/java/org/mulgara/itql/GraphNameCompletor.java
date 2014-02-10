/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */

package org.mulgara.itql;

import java.util.List;

import jline.Completor;

public class GraphNameCompletor implements Completor {
	
	private List<String> modelNames;
	
	public GraphNameCompletor(List<String> modelNames) {
		this.modelNames = modelNames;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public int complete(String s, int idx, List completionList) {
		int retValue = idx;
		
		if(s.endsWith("<")) {
			completionList.addAll(modelNames);
		} else {
			int gtIdx = s.lastIndexOf("<");
			boolean addedSomething = false;
		
			if(gtIdx >= 0) {
				String partial = s.substring(gtIdx+1);
				for(String s2 : modelNames) {
					if(s2.startsWith(partial)) {
						completionList.add(s2);
						addedSomething = true;
					}
				}
			}
			
			if(addedSomething) {
				retValue = gtIdx + 1;
			}
		}
		
		// TODO Auto-generated method stub
		return retValue ;
	}

}
