package login.extension;

import com.liferay.portal.kernel.exception.NoSuchUserException;
import com.liferay.portal.kernel.exception.RequiredReminderQueryException;
import com.liferay.portal.kernel.exception.UserActiveException;
import com.liferay.portal.kernel.exception.UserReminderQueryException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortletKeys;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.util.PropsValues;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletSession;

import static com.liferay.portal.kernel.util.PortalUtil.getUser;


@Component(
        immediate = true,
        property = {
                "javax.portlet.name=" + PortletKeys.FAST_LOGIN,
                "javax.portlet.name=" + PortletKeys.LOGIN,
                "mvc.command.name=/login/forgot_password",
                "service.ranking:Integer=1000"
        },
        service = MVCActionCommand.class
)
public class ForgotPasswordMVCActionCommand extends BaseMVCActionCommand {

    private static final Logger _logger = LoggerFactory.getLogger(ForgotPasswordMVCActionCommand.class);

    @Override
    protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
        int step = ParamUtil.getInteger(actionRequest, "step");
        if (step == 2) {

            User user = getUser(actionRequest);

            if (PropsValues.USERS_REMINDER_QUERIES_ENABLED) {
                if (PropsValues.USERS_REMINDER_QUERIES_REQUIRED &&
                        !hasReminderQuestions(user)) {

                    throw new RequiredReminderQueryException(
                            "No security queries or answers are defined for user " +
                                    user.getUserId());
                }

                for (int i = 2; i <= 5; i++) {
                    String nextAnswer = ParamUtil.getString(actionRequest, "answer" + i);
                    if (!user.getExpandoBridge().getAttribute("security-question-answer-" + i).toString()
                            .equals(nextAnswer)) {

                        PortletSession portletSession = actionRequest.getPortletSession();

                        portletSession.setAttribute(
                                WebKeys.FORGOT_PASSWORD_REMINDER_USER_EMAIL_ADDRESS,
                                user.getEmailAddress());

                        actionRequest.setAttribute(WebKeys.FORGOT_PASSWORD_REMINDER_USER, user);

                        throw new UserReminderQueryException();
                    }
                }
            }
        }

        mvcActionCommand.processAction(actionRequest, actionResponse);
    }

    private boolean hasReminderQuestions(User user){
        for (int i = 2; i <= 5; i++) {
            if (!user.getExpandoBridge().hasAttribute("security-question-answer-" + i)){
                return false;
            }
        }
        return true;
    }

    private User getUser(ActionRequest actionRequest)
            throws Exception {

        PortletSession portletSession = actionRequest.getPortletSession();

        ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
                WebKeys.THEME_DISPLAY);

        String sessionEmailAddress = (String)portletSession.getAttribute(
                WebKeys.FORGOT_PASSWORD_REMINDER_USER_EMAIL_ADDRESS);

        User user = null;

        if (Validator.isNotNull(sessionEmailAddress)) {
            user = UserLocalServiceUtil.getUserByEmailAddress(
                    themeDisplay.getCompanyId(), sessionEmailAddress);
        }
        else {
            long userId = ParamUtil.getLong(actionRequest, "userId");
            String screenName = ParamUtil.getString(
                    actionRequest, "screenName");
            String emailAddress = ParamUtil.getString(
                    actionRequest, "emailAddress");

            if (Validator.isNotNull(emailAddress)) {
                user = UserLocalServiceUtil.getUserByEmailAddress(
                        themeDisplay.getCompanyId(), emailAddress);
            }
            else if (Validator.isNotNull(screenName)) {
                user = UserLocalServiceUtil.getUserByScreenName(
                        themeDisplay.getCompanyId(), screenName);
            }
            else if (userId > 0) {
                user = UserLocalServiceUtil.getUserById(userId);
            }
            else {
                throw new NoSuchUserException();
            }
        }

        if (!user.isActive()) {
            throw new UserActiveException();
        }

        return user;
    }

    @Reference(target = "(&(mvc.command.name=/login/forgot_password)" +
            "(javax.portlet.name=com_liferay_login_web_portlet_LoginPortlet)" +
            "(component.name=com.liferay.login.web.internal.portlet.action.ForgotPasswordMVCActionCommand))")
    protected MVCActionCommand mvcActionCommand;
}
