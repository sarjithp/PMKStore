package com.pmk.util;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.compiere.util.Env;
import org.compiere.util.Language;

public class POSEnv {
	
	private static final String CTX = "context";
	
	public static synchronized Properties getCtx(HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		Properties ctx = (Properties) session.getAttribute(CTX);

		if (ctx == null) {
			// Double checking for the context for initialisation
			// Eliminates problem that rises due to multi-threaded access
			// Performance penalty happens only for newly initialised context
			synchronized (POSEnv.class) {
				ctx = (Properties) session.getAttribute(CTX);

				if (ctx == null) {
					ctx = new Properties();
					setLanguage(ctx);
					session.setAttribute(CTX, ctx);
					session.setMaxInactiveInterval(-1);
				}
			}
		}
		return ctx;
	}

	private static void setLanguage(Properties ctx) {
		Env.setContext(ctx, Env.LANGUAGE, Language.AD_Language_en_US);
	}
}
