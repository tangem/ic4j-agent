package org.ic4j.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.apache.commons.lang3.ArrayUtils;
import org.ic4j.agent.icp.IcpMethod;
import org.ic4j.agent.identity.Identity;
import org.ic4j.agent.identity.Signature;
import org.ic4j.agent.replicaapi.Envelope;
import org.ic4j.agent.replicaapi.QueryContent;
import org.ic4j.agent.requestid.RequestId;

import java.time.Duration;
import java.util.Arrays;

public class TangemUtils {

    public static byte[] constructEnvelopePayload(IcpMethod method, Identity identity) {
        QueryContent request = new QueryContent();
        request.queryRequest.methodName = method.getMethodName();
        request.queryRequest.arg = method.getArgs().toBytes();
        request.queryRequest.canisterId = method.getCanister();
        request.queryRequest.sender = identity.sender();
        request.queryRequest.ingressExpiry = getExpiryDate();

        RequestId requestId = RequestId.toRequestId(request);
        byte[] msg = constructMessage(requestId);
        Signature signature = identity.sign(msg);

        Envelope<QueryContent> envelope = new Envelope<QueryContent>();

        envelope.content = request;
        envelope.senderPubkey = signature.publicKey;
        envelope.senderSig = signature.signature;

        ObjectMapper objectMapper = new ObjectMapper(new CBORFactory()).registerModule(new Jdk8Module());
        ObjectWriter objectWriter = objectMapper.writerFor(Envelope.class).withAttribute("request_type", "query");

        try {
            return objectWriter.writeValueAsBytes(envelope);
        } catch (JsonProcessingException e) {
            System.err.println(e.getMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
            throw AgentError.create(AgentError.AgentErrorCode.INVALID_CBOR_DATA, e, envelope);
            // normally, rethrow exception here - or don't catch it at all.
        }
    }

    private static byte[] constructMessage(RequestId requestId) {
        return ArrayUtils.addAll(Agent.IC_REQUEST_DOMAIN_SEPARATOR, requestId.get());
    }

    private static Long getExpiryDate() {
        Duration permittedDrift = Duration.ofSeconds(Agent.DEFAULT_PERMITTED_DRIFT);

        return ((Duration.ofSeconds(Agent.DEFAULT_INGRESS_EXPIRY_DURATION)
                .plus(Duration.ofMillis(System.currentTimeMillis())))
                .minus(permittedDrift))
                .toNanos();
    }

}
