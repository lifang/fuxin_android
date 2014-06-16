package com.fuwu.mobileim.util;

import android.util.Log;

import com.fuwu.mobileim.model.Models.ContactRequest;
import com.fuwu.mobileim.model.Models.ContactResponse;
import com.fuwu.mobileim.pojo.ContactPojo;

/**
 * @作者 马龙
 * @时间 创建时间：2014-6-10 下午6:21:30
 */
public class ContactUtil {
	class getContacts implements Runnable {
		public void run() {
			try {
				ContactRequest.Builder builder = ContactRequest.newBuilder();
				// builder.setUserId(fxApplication.getUser_id());
				// builder.setToken(fxApplication.getToken());
				ContactRequest response = builder.build();

				byte[] by = HttpUtil.sendHttps(response.toByteArray(),
						Urlinterface.getContacts, "POST");
				if (by != null && by.length > 0) {

					ContactResponse res = ContactResponse.parseFrom(by);
					if (res.getIsSucceed()) {

						for (int i = 0; i < res.getContactsCount(); i++) {
							Log.i("linshi", res.getContactsCount() + "---" + i
									+ "---" + res.getContacts(i).getName());
							int contactId = res.getContacts(i).getContactId();
							String name = res.getContacts(i).getName();
							String customName = res.getContacts(i)
									.getCustomName();
							String sortKey = null;
							if (customName != null && customName.length() > 0) {
								// sortKey = findSortKey(customName);
							} else {
								// sortKey = findSortKey(name);
							}
							String userface_url = res.getContacts(i)
									.getTileUrl();
							int sex = res.getContacts(i).getGender()
									.getNumber();
							int source = res.getContacts(i).getSource();
							String lastContactTime = res.getContacts(i)
									.getLastContactTime();// 2014-05-27 11:42:18
							Boolean isblocked = res.getContacts(i)
									.getIsBlocked();
							Boolean isprovider = res.getContacts(i)
									.getIsProvider();
							int isBlocked = -1, isProvider = -1;
							if (isblocked == true) {
								isBlocked = 1;
							} else if (isblocked == false) {
								isBlocked = 0;
							}
							if (isprovider == true) {
								isProvider = 1;
							} else if (isprovider == false) {
								isProvider = 0;
							}

							String lisence = res.getContacts(i).getLisence();
							String individualResume = res.getContacts(i)
									.getIndividualResume();
							ContactPojo coPojo = new ContactPojo(contactId,
									sortKey, name, customName, userface_url,
									sex, source, lastContactTime, isBlocked,
									isProvider, lisence, individualResume);

						}
					} else {
					}

				} else {
				}

			} catch (Exception e) {
			}
		}
	}
}
