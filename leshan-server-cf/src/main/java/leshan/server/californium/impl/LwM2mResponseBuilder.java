/*
 * Copyright (c) 2013, Sierra Wireless
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of {{ project }} nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package leshan.server.californium.impl;

import leshan.LinkObject;
import leshan.ResponseCode;
import leshan.core.node.LwM2mNode;
import leshan.core.node.LwM2mPath;
import leshan.core.node.codec.InvalidValueException;
import leshan.core.node.codec.LwM2mNodeDecoder;
import leshan.core.request.ContentFormat;
import leshan.core.request.CreateRequest;
import leshan.core.request.DeleteRequest;
import leshan.core.request.DiscoverRequest;
import leshan.core.request.ExecuteRequest;
import leshan.core.request.DownlinkRequestVisitor;
import leshan.core.request.ObserveRequest;
import leshan.core.request.ReadRequest;
import leshan.core.request.WriteAttributesRequest;
import leshan.core.request.WriteRequest;
import leshan.core.response.CreateResponse;
import leshan.core.response.DiscoverResponse;
import leshan.core.response.LwM2mResponse;
import leshan.core.response.ValueResponse;
import leshan.server.client.Client;
import leshan.server.observation.ObservationRegistry;
import leshan.server.request.ResourceAccessException;
import leshan.util.Validate;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LwM2mResponseBuilder<T extends LwM2mResponse> implements DownlinkRequestVisitor {

    private static final Logger LOG = LoggerFactory.getLogger(LwM2mResponseBuilder.class);

    private LwM2mResponse lwM2mresponse;
    private final Request coapRequest;
    private final Response coapResponse;
    private final ObservationRegistry observationRegistry;
    private final Client client;

    public static ResponseCode fromCoapCode(final int code) {
        Validate.notNull(code);

        if (code == CoAP.ResponseCode.CREATED.value) {
            return ResponseCode.CREATED;
        } else if (code == CoAP.ResponseCode.DELETED.value) {
            return ResponseCode.DELETED;
        } else if (code == CoAP.ResponseCode.CHANGED.value) {
            return ResponseCode.CHANGED;
        } else if (code == CoAP.ResponseCode.CONTENT.value) {
            return ResponseCode.CONTENT;
        } else if (code == CoAP.ResponseCode.BAD_REQUEST.value) {
            return ResponseCode.BAD_REQUEST;
        } else if (code == CoAP.ResponseCode.UNAUTHORIZED.value) {
            return ResponseCode.UNAUTHORIZED;
        } else if (code == CoAP.ResponseCode.NOT_FOUND.value) {
            return ResponseCode.NOT_FOUND;
        } else if (code == CoAP.ResponseCode.METHOD_NOT_ALLOWED.value) {
            return ResponseCode.METHOD_NOT_ALLOWED;
        } else if (code == 137) {
            return ResponseCode.CONFLICT;
        } else {
            throw new IllegalArgumentException("Invalid CoAP code for LWM2M response: " + code);
        }
    }

    public LwM2mResponseBuilder(final Request coapRequest, final Response coapResponse, final Client client,
            final ObservationRegistry observationRegistry) {
        super();
        this.coapRequest = coapRequest;
        this.coapResponse = coapResponse;
        this.observationRegistry = observationRegistry;
        this.client = client;
    }

    @Override
    public void visit(final ReadRequest request) {
        switch (coapResponse.getCode()) {
        case CONTENT:
            lwM2mresponse = buildContentResponse(request.getPath(), coapResponse);
            break;
        case UNAUTHORIZED:
        case NOT_FOUND:
        case METHOD_NOT_ALLOWED:
            lwM2mresponse = new ValueResponse(fromCoapCode(coapResponse.getCode().value));
            break;
        default:
            handleUnexpectedResponseCode(client.getEndpoint(), coapRequest, coapResponse);
        }
    }

    @Override
    public void visit(final DiscoverRequest request) {
        switch (coapResponse.getCode()) {
        case CONTENT:
            LinkObject[] links = null;
            if (MediaTypeRegistry.APPLICATION_LINK_FORMAT != coapResponse.getOptions().getContentFormat()) {
                LOG.debug("Expected LWM2M Client [{}] to return application/link-format [{}] content but got [{}]",
                        client.getEndpoint(), MediaTypeRegistry.APPLICATION_LINK_FORMAT, coapResponse.getOptions()
                                .getContentFormat());
                links = new LinkObject[] {}; // empty list
            } else {
                links = LinkObject.parse(coapResponse.getPayload());
            }
            lwM2mresponse = new DiscoverResponse(fromCoapCode(coapResponse.getCode().value), links);
            break;
        case NOT_FOUND:
        case UNAUTHORIZED:
        case METHOD_NOT_ALLOWED:
            lwM2mresponse = new DiscoverResponse(fromCoapCode(coapResponse.getCode().value));
            break;
        default:
            handleUnexpectedResponseCode(client.getEndpoint(), coapRequest, coapResponse);
        }
    }

    @Override
    public void visit(final WriteRequest request) {
        switch (coapResponse.getCode()) {
        case CHANGED:
            lwM2mresponse = new LwM2mResponse(fromCoapCode(coapResponse.getCode().value));
            break;
        case BAD_REQUEST:
        case NOT_FOUND:
        case UNAUTHORIZED:
        case METHOD_NOT_ALLOWED:
            lwM2mresponse = new LwM2mResponse(fromCoapCode(coapResponse.getCode().value));
            break;
        default:
            handleUnexpectedResponseCode(client.getEndpoint(), coapRequest, coapResponse);
        }
    }

    @Override
    public void visit(final WriteAttributesRequest request) {
        switch (coapResponse.getCode()) {
        case CHANGED:
            lwM2mresponse = new LwM2mResponse(fromCoapCode(coapResponse.getCode().value));
            break;
        case BAD_REQUEST:
        case NOT_FOUND:
        case UNAUTHORIZED:
        case METHOD_NOT_ALLOWED:
            lwM2mresponse = new LwM2mResponse(fromCoapCode(coapResponse.getCode().value));
            break;
        default:
            handleUnexpectedResponseCode(client.getEndpoint(), coapRequest, coapResponse);
        }
    }

    @Override
    public void visit(final ExecuteRequest request) {
        switch (coapResponse.getCode()) {
        case CHANGED:
            lwM2mresponse = new LwM2mResponse(fromCoapCode(coapResponse.getCode().value));
            break;
        case BAD_REQUEST:
        case UNAUTHORIZED:
        case NOT_FOUND:
        case METHOD_NOT_ALLOWED:
            lwM2mresponse = new LwM2mResponse(fromCoapCode(coapResponse.getCode().value));
            break;
        default:
            handleUnexpectedResponseCode(client.getEndpoint(), coapRequest, coapResponse);
        }

    }

    @Override
    public void visit(final CreateRequest request) {
        switch (coapResponse.getCode()) {
        case CREATED:
            lwM2mresponse = new CreateResponse(fromCoapCode(coapResponse.getCode().value), coapResponse.getOptions()
                    .getLocationPathString());
            break;
        case BAD_REQUEST:
        case UNAUTHORIZED:
        case NOT_FOUND:
        case METHOD_NOT_ALLOWED:
            lwM2mresponse = new CreateResponse(fromCoapCode(coapResponse.getCode().value));
            break;
        default:
            handleUnexpectedResponseCode(client.getEndpoint(), coapRequest, coapResponse);
        }
    }

    @Override
    public void visit(final DeleteRequest request) {
        switch (coapResponse.getCode()) {
        case DELETED:
            lwM2mresponse = new LwM2mResponse(fromCoapCode(coapResponse.getCode().value));
            break;
        case UNAUTHORIZED:
        case NOT_FOUND:
        case METHOD_NOT_ALLOWED:
            lwM2mresponse = new LwM2mResponse(fromCoapCode(coapResponse.getCode().value));
            break;
        default:
            handleUnexpectedResponseCode(client.getEndpoint(), coapRequest, coapResponse);
        }
    }

    @Override
    public void visit(final ObserveRequest request) {
        switch (coapResponse.getCode()) {
        case CHANGED:
            // ignore changed response (this is probably a NOTIFY)
            lwM2mresponse = null;
            break;
        case CONTENT:
            lwM2mresponse = buildContentResponse(request.getPath(), coapResponse);
            if (coapResponse.getOptions().hasObserve()) {
                // observe request succeed so we can add and observation to registry
                final CaliforniumObservation observation = new CaliforniumObservation(coapRequest, client,
                        request.getPath());
                coapRequest.addMessageObserver(observation);
                observationRegistry.addObservation(observation);
            }
            break;
        case NOT_FOUND:
        case METHOD_NOT_ALLOWED:
            lwM2mresponse = new ValueResponse(fromCoapCode(coapResponse.getCode().value));
            break;
        default:
            handleUnexpectedResponseCode(client.getEndpoint(), coapRequest, coapResponse);
        }
    }

    private ValueResponse buildContentResponse(final LwM2mPath path, final Response coapResponse) {
        final ResponseCode code = ResponseCode.CONTENT;
        LwM2mNode content;
        try {
            content = LwM2mNodeDecoder.decode(coapResponse.getPayload(),
                    ContentFormat.fromCode(coapResponse.getOptions().getContentFormat()), path);
        } catch (final InvalidValueException e) {
            final String msg = String.format("[%s] ([%s])", e.getMessage(), e.getPath().toString());
            throw new ResourceAccessException(code, path.toString(), msg, e);
        }
        return new ValueResponse(code, content);
    }

    @SuppressWarnings("unchecked")
    public T getResponse() {
        return (T) lwM2mresponse;
    }

    /**
     * Throws a generic {@link ResourceAccessException} indicating that the client returned an unexpected response code.
     *
     * @param request
     * @param coapRequest
     * @param coapResponse
     */
    private void handleUnexpectedResponseCode(final String clientEndpoint, final Request coapRequest,
            final Response coapResponse) {
        final String msg = String.format("Client [%s] returned unexpected response code [%s]", clientEndpoint,
                coapResponse.getCode());
        throw new ResourceAccessException(fromCoapCode(coapResponse.getCode().value), coapRequest.getURI(), msg);
    }
}
