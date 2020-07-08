package org.oddjob.jmx.server;

import org.oddjob.remote.HasOperationType;
import org.oddjob.remote.OperationType;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import java.util.ArrayList;
import java.util.List;

public class JMXOperationPlus<T> extends JMXOperation<T> implements HasOperationType<T> {

    private final String actionName;

    private final Class<T> returnType;

    private final String description;

    private final int impact;

    private final List<Param> params;

    private final OperationType<T> operationType;

    public JMXOperationPlus(String actionName,
                            String description,
                            Class<T> returnType,
                            int impact) {
        this(actionName, description, returnType, impact,
                new ArrayList<>());
    }

    private JMXOperationPlus(String actionName,
                             String description,
                             Class<T> returnType,
                             int impact,
                             List<Param> params) {

        this.actionName = actionName;
        this.returnType = returnType;
        this.description = description;
        this.impact = impact;
        this.params = new ArrayList<>(params);

        Class<?>[] signature = params.stream().map(p -> p.type).toArray(Class[]::new);
        this.operationType = new OperationType<>(actionName, signature, returnType);
    }

    public String getActionName() {
        return actionName;
    }

    public String[] getSignature() {
        String[] signature = new String[params.size()];
        int i = 0;
        for (Param param : params) {
            signature[i++] = param.type.getName();
        }
        return signature;
    }

    public MBeanOperationInfo getOpInfo() {
        MBeanParameterInfo[] paramInfos = new MBeanParameterInfo[params.size()];
        int i = 0;
        for (Param param : params) {
            paramInfos[i++] = new MBeanParameterInfo(
                    param.name, param.type.getName(), param.description);
        }
        return new MBeanOperationInfo(
                actionName,
                description,
                paramInfos,
                returnType.getName(),
                impact);
    }

    public JMXOperationPlus<T> addParam(String name, Class<?> type, String description) {
        List<Param> copy = new ArrayList<>(params);
        copy.add(new Param(name, type, description));

        return new JMXOperationPlus<>(
                this.actionName,
                this.description,
                this.returnType,
                this.impact,
                copy);
    }

    static class Param {
        final String name;
        final Class<?> type;
        final String description;

        Param(String name, Class<?> type, String description) {
            this.name = name;
            this.type = type;
            this.description = description;
        }
    }

    @Override
    public OperationType<T> getOperationType() {
        return operationType;
    }
}
