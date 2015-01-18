package com.miniand.brdgme;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;

/**
 * Created by beefsack on 25/12/14.
 */
public class CodeStringRequest extends Request<CodeStringRequest.CodeString> {
    public class CodeString {
        public CodeString(int code, String string) {
            this.code = code;
            this.string = string;
        }
        int code;
        String string;
    }

    private final Response.Listener<CodeString> mListener;

    public CodeStringRequest(int method, String url, Response.Listener<CodeString> listener,
                             Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
    }

    @Override
    protected void deliverResponse(CodeString response) {
        mListener.onResponse(response);
    }

    @Override
    protected Response<CodeString> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(new CodeString(response.statusCode, parsed), HttpHeaderParser.parseCacheHeaders(response));
    }
}
