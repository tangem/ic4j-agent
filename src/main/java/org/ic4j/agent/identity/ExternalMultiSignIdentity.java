package org.ic4j.agent.identity;

import org.ic4j.agent.AgentError;
import org.ic4j.agent.replicaapi.Delegation;
import org.ic4j.types.Principal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ExternalMultiSignIdentity extends Identity {

    public byte[] derEncodedPublickey;
    Function<List<byte[]>, List<byte[]>> signatureFunction;

    public ExternalMultiSignIdentity(
            byte[] derEncodedPublickey,
            Function<List<byte[]>, List<byte[]>> signatureFunction
    ) {
        this.derEncodedPublickey = derEncodedPublickey;
        this.signatureFunction = signatureFunction;
    }

    @Override
    public Principal sender() {
        return Principal.selfAuthenticating(derEncodedPublickey);
    }


    @Override
    public Signature sign(byte[] content) {
        return this.signArbitrary(content);
    }

    @Override
    public Signature signDelegation(Delegation delegation) throws AgentError {
        return this.signArbitrary(delegation.signable());
    }

    @Override
    public Signature signArbitrary(byte[] content) {
        throw new RuntimeException("Trying to sign single hash with MultiSingIdentity");
    }

    public List<Signature> multiSignArbitrary(List<byte[]> contents) {
        List<byte[]> signaturesBytes = signatureFunction.apply(contents);

        List<Signature> signatures = new ArrayList<>();
        for (byte[] signatureBytes : signaturesBytes) {
            signatures.add(new Signature(this.derEncodedPublickey, signatureBytes, null));
        }

        return signatures;
    }

    public byte[] getPublicKey()
    {
        return this.derEncodedPublickey;
    }
}
