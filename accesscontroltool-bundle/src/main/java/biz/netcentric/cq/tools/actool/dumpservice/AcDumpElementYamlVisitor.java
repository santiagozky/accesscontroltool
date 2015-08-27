/*
 * (C) Copyright 2015 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.cq.tools.actool.dumpservice;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import biz.netcentric.cq.tools.actool.authorizableutils.AuthorizableConfigBean;
import biz.netcentric.cq.tools.actool.helper.AcHelper;
import biz.netcentric.cq.tools.actool.helper.AceBean;

public class AcDumpElementYamlVisitor implements AcDumpElementVisitor {

    private final static int PRINCIPAL_BASED_SORTING = 1;
    private final static int PATH_BASED_SORTING = 2;

    public static final int DUMP_INDENTATION_KEY = 4;
    public static final int DUMP_INDENTATION_FIRST_PROPERTY = 7;
    public static final int DUMP_INDENTATION_PROPERTY = 9;

    public static final String YAML_STRUCTURAL_ELEMENT_PREFIX = "- ";
    private final StringBuilder sb;
    private final int mapOrder;

    public AcDumpElementYamlVisitor(final int mapOrder, final StringBuilder sb) {
        this.mapOrder = mapOrder;
        this.sb = sb;
    }

    @Override
    public void visit(final AuthorizableConfigBean authorizableConfigBean) {
        sb.append(AcHelper.getBlankString(DUMP_INDENTATION_KEY))
                .append("- " + authorizableConfigBean.getPrincipalID() + ":")
                .append("\n");
        sb.append("\n");
        sb.append(AcHelper.getBlankString(DUMP_INDENTATION_FIRST_PROPERTY))
                .append("- name: ").append("\n");
        sb.append(AcHelper.getBlankString(DUMP_INDENTATION_PROPERTY))
                .append("memberOf: "
                        + authorizableConfigBean.getMemberOfString())
                .append("\n");
        sb.append(AcHelper.getBlankString(DUMP_INDENTATION_PROPERTY))
                .append("path: " + authorizableConfigBean.getPath())
                .append("\n");
        sb.append(AcHelper.getBlankString(DUMP_INDENTATION_PROPERTY))
                .append("isGroup: " + "'" + authorizableConfigBean.isGroup()
                        + "'").append("\n");
        sb.append("\n");
    }

    @Override
    public void visit(final AceBean aceBean) {

        if (mapOrder == PATH_BASED_SORTING) {
            sb.append(AcHelper.getBlankString(DUMP_INDENTATION_FIRST_PROPERTY))
                    .append("- principal: " + aceBean.getPrincipalName())
                    .append("\n");
        } else if (mapOrder == PRINCIPAL_BASED_SORTING) {
            sb.append(AcHelper.getBlankString(DUMP_INDENTATION_FIRST_PROPERTY))
                    .append("- path: " + aceBean.getJcrPath()).append("\n");
        }
        sb.append(AcHelper.getBlankString(DUMP_INDENTATION_PROPERTY))
                .append("permission: " + aceBean.getPermission()).append("\n");
        sb.append(AcHelper.getBlankString(DUMP_INDENTATION_PROPERTY))
                .append("actions: " + aceBean.getActionsString()).append("\n");
        sb.append(AcHelper.getBlankString(DUMP_INDENTATION_PROPERTY))
                .append("privileges: " + aceBean.getPrivilegesString())
                .append("\n");
        
        appendRestrictions(aceBean);
                
        sb.append("\n");
        sb.append("\n");
    }

	private void appendRestrictions(final AceBean aceBean) {
		Map<String, String> restrictionsMap = aceBean.getRestrictionsMap();
		for(String restrictionName : restrictionsMap.keySet()){
			final String restrictionValue = restrictionsMap.get(restrictionName);
        	if(restrictionValue != null){
        		restrictionName = getCamelCaseRestrictionName(restrictionName);
        		sb.append(AcHelper.getBlankString(DUMP_INDENTATION_PROPERTY))
                .append(restrictionName + ": " + "'" + restrictionValue  + "'")
                .append("\n");
        	}
        }
	}
	
    /**
     * gets rid of the colon (':') and returns a camel case version of the actual restriction name for usage in yaml file (e.g. rep:glob -> regGlob) since
     * colon is a special yaml character
     * @param restrictionName actual restriction name
     * @return camel case version
     */
	private String getCamelCaseRestrictionName(String restrictionName) {
		String[] restrictionNamePartialStrings = restrictionName.split(":");
		restrictionNamePartialStrings[1] = restrictionNamePartialStrings[1].substring(0, 1).toUpperCase() +  restrictionNamePartialStrings[1].substring(1);
		restrictionName = restrictionNamePartialStrings[0] + restrictionNamePartialStrings[1];
		return restrictionName;
	}

    @Override
    public void visit(final CommentingDumpElement commentingDumpElement) {
        sb.append(CommentingDumpElement.YAML_COMMENT_PREFIX
                + commentingDumpElement.getString());
        sb.append("\n");
        sb.append("\n");
    }

    @Override
    public void visit(final StructuralDumpElement structuralDumpElement) {
        sb.append("\n");
        sb.append(AcHelper.getBlankString(structuralDumpElement.getLevel() * 2)
                + YAML_STRUCTURAL_ELEMENT_PREFIX
                + structuralDumpElement.getString()
                + MapKey.YAML_MAP_KEY_SUFFIX);
        sb.append("\n");
        sb.append("\n");
    }
}
