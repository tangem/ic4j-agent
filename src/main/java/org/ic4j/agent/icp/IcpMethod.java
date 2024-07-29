package org.ic4j.agent.icp;

import org.ic4j.candid.IDLUtils;
import org.ic4j.candid.parser.IDLArgs;
import org.ic4j.candid.parser.IDLValue;
import org.ic4j.candid.types.Label;
import org.ic4j.candid.types.Type;
import org.ic4j.types.Principal;

import java.util.ArrayList;
import java.util.HashMap;

public class IcpMethod {

    Principal canister;
    String methodName;
    IDLArgs args;

    public IcpMethod(Principal canister, String methodName, IDLArgs args) {
        this.canister = canister;
        this.methodName = methodName;
        this.args = args;
    }

    public static IcpMethod balance(Byte[] account) {
        HashMap<Label, Byte[]> map = new HashMap<>();
        map.put(Label.createIdLabel(IDLUtils.idlHash("account")), account);
        IDLValue record = IDLValue.create(map, Type.RECORD);
        ArrayList<IDLValue> args = new ArrayList<>();
        args.add(record);
        return new IcpMethod(IcpSystemCanisters.LEDGER, "account_balance", IDLArgs.create(args));
    }

    public Principal getCanister() {
        return canister;
    }

    public String getMethodName() {
        return methodName;
    }

    public IDLArgs getArgs() {
        return args;
    }
}
