package leshan.client.lwm2m.coap.californium;

import java.util.Map.Entry;

import leshan.client.lwm2m.resource.LinkFormattable;
import leshan.client.lwm2m.resource.LwM2mClientObjectInstance;
import leshan.client.lwm2m.resource.LwM2mClientResource;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

public class CaliforniumBasedObjectInstance extends CaliforniumBasedLwM2mNode<LwM2mClientObjectInstance> implements LinkFormattable {

	public CaliforniumBasedObjectInstance(final int instanceId, final LwM2mClientObjectInstance instance) {
		super(instanceId, instance);
		for (final Entry<Integer, LwM2mClientResource> entry : instance.getAllResources().entrySet()) {
			final Integer resourceId = entry.getKey();
			final LwM2mClientResource resource = entry.getValue();
			add(new CaliforniumBasedResource(resourceId, resource));
		}
	}

	@Override
	public void handleDELETE(final CoapExchange exchange) {
		getParent().remove(this);

		exchange.respond(ResponseCode.DELETED);
	}

	@Override
	public String asLinkFormat() {
		final StringBuilder linkFormat = LinkFormat.serializeResource(this).append(LinkFormat.serializeAttributes(getAttributes()));
		for(final Resource child : getChildren()) {
			linkFormat.append(LinkFormat.serializeResource(child));
		}
		linkFormat.deleteCharAt(linkFormat.length() - 1);

		return linkFormat.toString();
	}

}