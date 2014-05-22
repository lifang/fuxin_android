// See README.txt for information and build instructions.
package com.fuwu.mobileim.activity;

import com.fuwu.mobileim.model.Models.AuthenticationRequest;
import com.fuwu.mobileim.model.Models.AuthenticationResponse;
import com.fuwu.mobileim.model.Models.MessageInfo;
import com.google.protobuf.InvalidProtocolBufferException;

class ProtocolBufferDemo {
	public byte[] x() {
		AuthenticationResponse response = null;
		try {
			byte[] authenticationRequest = null;
			boolean result = false;
			AuthenticationRequest request = AuthenticationRequest
					.parseFrom(authenticationRequest);
			if (request.getUserName() == "MockUserName") {
				result = true;
			}
			AuthenticationResponse.Builder builder = AuthenticationResponse
					.newBuilder();
			builder.setToken("aa");
			builder.setUserId(1);
			builder.setIsSucceed(true);

			response = builder.build();

		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		return response.toByteArray();
	}
}
