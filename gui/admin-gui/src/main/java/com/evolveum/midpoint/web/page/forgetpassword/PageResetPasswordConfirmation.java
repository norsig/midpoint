package com.evolveum.midpoint.web.page.forgetpassword;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.evolveum.midpoint.gui.api.page.PageBase;
import com.evolveum.midpoint.gui.api.util.WebModelServiceUtils;
import com.evolveum.midpoint.model.api.AuthenticationEvaluator;
import com.evolveum.midpoint.prism.delta.ContainerDelta;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.result.OperationResultStatus;
import com.evolveum.midpoint.schema.util.ObjectTypeUtil;
import com.evolveum.midpoint.security.api.ConnectionEnvironment;
import com.evolveum.midpoint.security.api.MidPointPrincipal;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.Producer;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.application.PageDescriptor;
import com.evolveum.midpoint.web.component.util.VisibleEnableBehaviour;
import com.evolveum.midpoint.web.page.login.PageLogin;
import com.evolveum.midpoint.web.page.login.PageRegistrationBase;
import com.evolveum.midpoint.web.page.login.PageRegistrationConfirmation;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AssignmentType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.CredentialsType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.NonceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;

@PageDescriptor(url = "/resetPasswordConfrimation")
public class PageResetPasswordConfirmation extends PageRegistrationBase{

	
private static final Trace LOGGER = TraceManager.getTrace(PageRegistrationConfirmation.class);
	
	private static final String DOT_CLASS = PageRegistrationConfirmation.class.getName() + ".";

	
	private static final String ID_LABEL_ERROR = "errorLabel";
	private static final String ID_ERROR_PANEL = "errorPanel";
	
	private static final String OPERATION_ASSIGN_DEFAULT_ROLES = DOT_CLASS + ".assignDefaultRoles";
	private static final String OPERATION_FINISH_REGISTRATION = DOT_CLASS + "finishRegistration";

	private static final long serialVersionUID = 1L;

	public PageResetPasswordConfirmation() {
		super();
		init(null);
	}

	public PageResetPasswordConfirmation(PageParameters params) {
		super();
		init(params);
	}

	private void init(final PageParameters pageParameters) {

		PageParameters params = pageParameters;
		if (params == null) {
			params = getPageParameters();
		}
		
		OperationResult result = new OperationResult(OPERATION_FINISH_REGISTRATION);
		if (params == null) {
			LOGGER.error("Confirmation link is not valid. No credentials provided in it");
			String msg = createStringResource("PageSelfRegistration.invalid.registration.link").getString();
			getSession().error(createStringResource(msg));
			result.recordFatalError(msg);
			initLayout(result);
			return;
		}

		StringValue userNameValue = params.get(SchemaConstants.RESET_PASSWORD_ID);
		Validate.notEmpty(userNameValue.toString());
		StringValue tokenValue = params.get(SchemaConstants.RESET_PASSWORD_TOKEN);
		Validate.notEmpty(tokenValue.toString());
			
		UsernamePasswordAuthenticationToken token = authenticateUser(userNameValue.toString(), tokenValue.toString(), result);
		if (token == null) {
			initLayout(result);
			return;
		} else {
			SecurityContextHolder.getContext().setAuthentication(token);
			setResponsePage(PageResetPassword.class);
		}

		initLayout(result);
	}
	
	private UsernamePasswordAuthenticationToken authenticateUser(String username, String nonce, OperationResult result){
		ConnectionEnvironment connEnv = new ConnectionEnvironment();
		connEnv.setChannel(SchemaConstants.CHANNEL_GUI_SELF_REGISTRATION_URI);
		try {
			return getAuthenticationEvaluator().authenticateUserNonce(connEnv, username,
					nonce, getResetPasswordPolicy().getNoncePolicy());
		} catch (AuthenticationException ex) {
			getSession()
					.error(getString(ex.getMessage()));
			result.recordFatalError("Failed to validate user");
			LoggingUtils.logException(LOGGER, ex.getMessage(), ex);
			 return null;
		} catch (Exception ex) {
			getSession()
			.error(createStringResource("PageRegistrationConfirmation.authnetication.failed").getString());
			LoggingUtils.logException(LOGGER, "Failed to confirm registration", ex);
			return null;
		}
	}
	
	
	
	
	private void initLayout(final OperationResult result) {

		WebMarkupContainer errorPanel = new WebMarkupContainer(ID_ERROR_PANEL);
		add(errorPanel);
		errorPanel.add(new VisibleEnableBehaviour() {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled() {
				return result.getStatus() == OperationResultStatus.FATAL_ERROR;
			}

			@Override
			public boolean isVisible() {
				return result.getStatus() == OperationResultStatus.FATAL_ERROR;
			}
		});
		Label errorMessage = new Label(ID_LABEL_ERROR,
				createStringResource("PageRegistrationConfirmation.confirmation.error"));
		errorPanel.add(errorMessage);

	}
	
	@Override
	protected void createBreadcrumb() {
		// don't create breadcrumb for registration confirmation page
	}
}
