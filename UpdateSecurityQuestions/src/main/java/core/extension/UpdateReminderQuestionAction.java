package core.extension;

import com.liferay.expando.kernel.exception.DuplicateColumnNameException;
import com.liferay.expando.kernel.exception.DuplicateTableNameException;
import com.liferay.expando.kernel.model.ExpandoColumn;
import com.liferay.expando.kernel.model.ExpandoColumnConstants;
import com.liferay.expando.kernel.model.ExpandoTable;
import com.liferay.expando.kernel.service.ExpandoColumnLocalServiceUtil;
import com.liferay.expando.kernel.service.ExpandoTableLocalServiceUtil;
import com.liferay.expando.kernel.service.ExpandoValueLocalServiceUtil;
import com.liferay.portal.action.UpdateReminderQueryAction;
import com.liferay.portal.kernel.exception.NoSuchUserException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.exception.UserReminderQueryException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.ClassedModel;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.AuthTokenUtil;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserServiceUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.struts.BaseStrutsAction;
import com.liferay.portal.kernel.struts.StrutsAction;
import com.liferay.portal.kernel.util.*;
import com.liferay.portal.struts.ActionConstants;
import org.osgi.service.component.annotations.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Liferay
 */
@Component(
        immediate = true, property = {"path=/portal/update_reminder_query"},
        service = StrutsAction.class
)
public class UpdateReminderQuestionAction extends BaseStrutsAction {

    private static final Log log = LogFactoryUtil.getLog(UpdateReminderQuestionAction.class);

    @Override
    public String execute(StrutsAction originalStrutsAction,
                          HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String cmd = ParamUtil.getString(request, Constants.CMD);

        if (Validator.isNull(cmd)) {
            return "portal.update_reminder_query";
        }

        try {
            updateReminderQuery(request, response);

            return ActionConstants.COMMON_REFERER_JSP;
        } catch (Exception e) {
            if (e instanceof UserReminderQueryException) {
                SessionErrors.add(request, e.getClass());

                return "portal.update_reminder_query";
            } else if (e instanceof NoSuchUserException ||
                    e instanceof PrincipalException) {

                SessionErrors.add(request, e.getClass());

                return "portal.error";
            }

            PortalUtil.sendError(e, request, response);

            return null;
        }
    }

    protected void updateReminderQuery(
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        AuthTokenUtil.checkCSRFToken(
                request, UpdateReminderQueryAction.class.getName());

        boolean empty = false;
        for (int i = 1; i <= 5; i++) {
            String answer = ParamUtil.getString(request, "reminderQueryAnswer" + i);
            if (answer.equals("")) {
                empty = true;
            }
        }

        if (empty) {
            throw new UserReminderQueryException();
        }

        long userId = PortalUtil.getUserId(request);
        String question = ParamUtil.getString(request, "reminderQueryQuestion1");
        String answer = ParamUtil.getString(request, "reminderQueryAnswer1");

        UserServiceUtil.updateReminderQuery(userId, question, answer);

        ExpandoTable userTable = getTable(PortalUtil.getDefaultCompanyId(), User.class);

        String[] guestActionIds = new String[]{ActionKeys.VIEW};
        String[] userActionIds = new String[]{ActionKeys.VIEW, ActionKeys.UPDATE};
        long guestRoleId =
                RoleLocalServiceUtil.getRole(PortalUtil.getDefaultCompanyId(), RoleConstants.GUEST).getRoleId();
        long userRoleId =
                RoleLocalServiceUtil.getRole(PortalUtil.getDefaultCompanyId(), RoleConstants.USER).getRoleId();

        for (int i = 2; i <= 5; i++) {
            ExpandoColumn questionColumn =
                    addExpandoColumn(PortalUtil.getDefaultCompanyId(), User.class, userTable, "security-question-" + i);
            ExpandoColumn answerColumn = addExpandoColumn(PortalUtil.getDefaultCompanyId(), User.class, userTable,
                    "security-question-answer-" + i);

            ExpandoValueLocalServiceUtil
                    .addValue(PortalUtil.getDefaultCompanyId(), User.class.getName(), userTable.getName(),
                            "security-question-" + i, userId,
                            ParamUtil.getString(request, "reminderQueryQuestion" + i));
            ExpandoValueLocalServiceUtil
                    .addValue(PortalUtil.getDefaultCompanyId(), User.class.getName(), userTable.getName(),
                            "security-question-answer-" + i, userId,
                            ParamUtil.getString(request, "reminderQueryAnswer" + i));

            ResourcePermissionLocalServiceUtil.setResourcePermissions(PortalUtil.getDefaultCompanyId(),
                    ExpandoColumn.class.getName(), ResourceConstants.SCOPE_INDIVIDUAL,
                    String.valueOf(questionColumn.getColumnId()), guestRoleId, guestActionIds);

            ResourcePermissionLocalServiceUtil.setResourcePermissions(PortalUtil.getDefaultCompanyId(),
                    ExpandoColumn.class.getName(), ResourceConstants.SCOPE_INDIVIDUAL,
                    String.valueOf(answerColumn.getColumnId()), guestRoleId, guestActionIds);

            ResourcePermissionLocalServiceUtil.setResourcePermissions(PortalUtil.getDefaultCompanyId(),
                    ExpandoColumn.class.getName(), ResourceConstants.SCOPE_INDIVIDUAL,
                    String.valueOf(questionColumn.getColumnId()), userRoleId, userActionIds);

            ResourcePermissionLocalServiceUtil.setResourcePermissions(PortalUtil.getDefaultCompanyId(),
                    ExpandoColumn.class.getName(), ResourceConstants.SCOPE_INDIVIDUAL,
                    String.valueOf(answerColumn.getColumnId()), userRoleId, userActionIds);
        }
    }

    private ExpandoTable getTable(long companyId, Class<? extends ClassedModel> model)
            throws SystemException, PortalException {
        ExpandoTable table;
        try {
            table = ExpandoTableLocalServiceUtil.addDefaultTable(companyId, model.getName());
        } catch (DuplicateTableNameException dtne) {
            table = ExpandoTableLocalServiceUtil.getDefaultTable(companyId, model.getName());
        }
        return table;
    }

    private ExpandoColumn addExpandoColumn(long companyId, Class<? extends ClassedModel> model, ExpandoTable table,
                                           String columnName)
            throws SystemException, PortalException {

        ExpandoColumn column;
        try {
            column = addColumn(table, columnName);
            log.info("Custom field " + columnName + " added for model " + model.getName());
        } catch (DuplicateColumnNameException dcne) {
            column = getColumn(table, columnName);
            log.debug("Custom field " + columnName + " already exists for model " + model.getName());
        }
        updateTypeSettings(column);
        return column;
    }

    private ExpandoColumn addColumn(ExpandoTable table, String columnName)
            throws PortalException, SystemException {
        final ExpandoColumn column = ExpandoColumnLocalServiceUtil
                .addColumn(table.getTableId(), columnName, ExpandoColumnConstants.STRING);
        return column;
    }

    private ExpandoColumn getColumn(ExpandoTable table, String columnName)
            throws SystemException {
        final ExpandoColumn column = ExpandoColumnLocalServiceUtil
                .getColumn(table.getTableId(), columnName);
        return column;
    }

    private void updateTypeSettings(final ExpandoColumn column)
            throws PortalException, SystemException {
        UnicodeProperties properties = column.getTypeSettingsProperties();
        // update the properties here
        ExpandoColumnLocalServiceUtil.updateTypeSettings(column.getColumnId(), properties.toString());
    }
}
