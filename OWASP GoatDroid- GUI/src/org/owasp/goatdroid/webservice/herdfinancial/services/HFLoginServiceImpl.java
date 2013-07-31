/**
 * OWASP GoatDroid Project
 * 
 * This file is part of the Open Web Application Security Project (OWASP)
 * GoatDroid project. For details, please see
 * https://www.owasp.org/index.php/Projects/OWASP_GoatDroid_Project
 *
 * Copyright (c) 2012 - The OWASP Foundation
 * 
 * GoatDroid is published by OWASP under the GPLv3 license. You should read and accept the
 * LICENSE before you use, modify, and/or redistribute this software.
 * 
 * @author Jack Mannino (Jack.Mannino@owasp.org https://www.owasp.org/index.php/User:Jack_Mannino)
 * @created 2012
 */
package org.owasp.goatdroid.webservice.herdfinancial.services;

import java.util.ArrayList;

import javax.annotation.Resource;

import org.owasp.goatdroid.webservice.herdfinancial.Constants;
import org.owasp.goatdroid.webservice.herdfinancial.Utils;
import org.owasp.goatdroid.webservice.herdfinancial.Validators;
import org.owasp.goatdroid.webservice.herdfinancial.dao.HFLoginDaoImpl;
import org.owasp.goatdroid.webservice.herdfinancial.model.LoginModel;
import org.springframework.stereotype.Service;

@Service
public class HFLoginServiceImpl implements LoginService {

	@Resource
	HFLoginDaoImpl dao;

	public boolean isAuthValid(String authToken) {

		if (!Validators.validateSessionTokenFormat(authToken))
			return false;

		boolean success = false;

		try {
			long sessionStart = dao.getSessionStartTime(sessionToken);
			if (Utils.getTimeMilliseconds() - sessionStart < Constants.MILLISECONDS_MONTH) {
				success = true;
			}
		} catch (Exception e) {

		} finally {
			try {
			} catch (Exception e) {
			}
		}
		return success;
	}

	public LoginModel isSessionValidOrDeviceAuthorized(String authToken,
			String deviceID) {

		LoginModel bean = new LoginModel();
		ArrayList<String> errors = new ArrayList<String>();
		if (!Validators.validateDeviceID(deviceID))
			errors.add(Constants.INVALID_DEVICE_ID);

		try {
			if (errors.size() == 0) {
				if (isSessionValid(authToken)) {
					bean.setSuccess(true);
				} else {
					if (dao.isDevicePermanentlyAuthorized(deviceID)) {
						int newSessionToken = Utils.generateSessionToken();
						dao.updateAuthorizedDeviceSession(deviceID,
								newSessionToken, Utils.getTimeMilliseconds());
						bean.setSessionToken(newSessionToken);
						bean.setUserName(dao.getUserName(newSessionToken));
						bean.setAccountNumber(dao
								.getAccountNumber(newSessionToken));
						bean.setSuccess(true);
					}
				}
			}
		} catch (Exception e) {
			errors.add(Constants.UNEXPECTED_ERROR);
		} finally {
			bean.setErrors(errors);
		}
		return bean;
	}

	public LoginModel validateCredentials(String userName, String password) {

		LoginModel login = new LoginModel();
		ArrayList<String> errors = Validators.validateCredentials(userName,
				password);

		try {
			if (errors.size() == 0) {
				if (dao.validateCredentials(userName, password)) {
					int sessionToken = Utils.generateSessionToken();
					dao.updateSession(userName, sessionToken,
							Utils.getTimeMilliseconds());
					login.setSessionToken(sessionToken);
					login.setUserName(dao.getUserName(sessionToken));
					login.setAccountNumber(dao.getAccountNumber(sessionToken));
					login.setSuccess(true);
				} else
					errors.add(Constants.INVALID_CREDENTIALS);
			}
		} catch (Exception e) {
			errors.add(Constants.UNEXPECTED_ERROR);
		} finally {
			login.setErrors(errors);
		}
		return login;
	}

	public LoginModel isDevicePermanentlyAuthorized(String deviceID) {

		LoginModel login = new LoginModel();
		ArrayList<String> errors = new ArrayList<String>();

		if (!Validators.validateDeviceID(deviceID))
			errors.add(Constants.INVALID_DEVICE_ID);

		try {
			if (errors.size() == 0) {
				if (dao.isDevicePermanentlyAuthorized(deviceID)) {
					int sessionToken = Utils.generateSessionToken();
					dao.updateAuthorizedDeviceSession(deviceID, sessionToken,
							Utils.getTimeMilliseconds());
					login.setSessionToken(sessionToken);
					login.setSuccess(true);
				}
			}
		} catch (Exception e) {
			errors.add(Constants.UNEXPECTED_ERROR);
		} finally {
			login.setErrors(errors);
		}
		return login;
	}
}