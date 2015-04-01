/**
 * $Id: $
 */
package org.eso.sdd.mbse.ls.validation;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.validation.ElementValidationRuleImpl;
import com.nomagic.magicdraw.validation.SmartListenerConfigurationProvider;
import com.nomagic.uml2.ext.jmi.smartlistener.SmartListenerConfig;
import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.Interface;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;

import java.lang.Class;
import java.util.*;

/**
 * This is an example of binary validation rule. The rule checks whether all
 * operations which are setters (starts with prefix "set") has input parameters.
 *
 * @author Rimvydas Vaidelis
 * @version $Revision: $, $Date: $
 */
public class MyBinaryValidationRuleImpl implements ElementValidationRuleImpl, SmartListenerConfigurationProvider
{
    /**
     * Solving actions.
     */
    private List<NMAction> mActions;

    /**
     * Default constructor. Default constructor is required.
     */
    public MyBinaryValidationRuleImpl()
    {
        mActions = new ArrayList<NMAction>();
        NMAction action = new MyAction("NO_INPUT_PARAMETERS", "No Input Parameters", null);
        mActions.add(action);
    }


    @Override
	public void init(Project project, Constraint constraint)
    {
    }


    /**
     * Returns a map of classes and smart listener configurations.
     *
     * @return smart listener configurations.
     */
    @Override
	public Map<Class<? extends Element>, Collection<SmartListenerConfig>> getListenerConfigurations()
    {
        Map<Class<? extends Element>, Collection<SmartListenerConfig>> configMap =
                new HashMap<Class<? extends Element>, Collection<SmartListenerConfig>>();
        SmartListenerConfig config = new SmartListenerConfig();
        SmartListenerConfig nested = config.listenToNested("ownedOperation");
        nested.listenTo("name");
        nested.listenTo("ownedParameter");

        Collection<SmartListenerConfig> configs = Collections.singletonList(config);
        configMap.put(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, configs);
        configMap.put(Interface.class, configs);
        return configMap;
    }

    /**
     * Executes the rule.
     *
     * @param project    a project of the constraint.
     * @param constraint constraint which defines validation rules.
     * @param elements   collection of elements that have to be validated.
     * @return a set of <code>Annotation</code> objects which specifies invalid objects.
     */
    @Override
	public Set<Annotation> run(Project project, Constraint constraint, Collection<? extends Element> elements)
    {
        Set<Annotation> result = new HashSet<Annotation>();
        for (Element element : elements)
        {
            if (element instanceof Classifier )
            {
                Classifier classifier = (Classifier) element;
                Collection<Feature> features = classifier.getFeature();
                for (Feature feature : features)
                {
                    if (feature instanceof Operation)
                    {
                        Operation operation = (Operation) feature;
                        String name = operation.getName();
                        if (name != null && name.startsWith("set"))
                        {
                            List<Parameter> parameters = operation.getOwnedParameter();
                            boolean hasInput = false;
                            for (Parameter parameter : parameters)
                            {
                                ParameterDirectionKind parameterDirectionKind = parameter.getDirection();
                                if (ParameterDirectionKindEnum.IN.equals(parameterDirectionKind) ||
                                    ParameterDirectionKindEnum.INOUT.equals(parameterDirectionKind))
                                {
                                    hasInput = true;
                                    break;
                                }
                            }
                            if (!hasInput)
                            {
                                Annotation annotation = new Annotation(element, constraint, mActions);
                                result.add(annotation);
                            }
                        }

                    }

                }
            }
        }
        return result;
    }

    @Override
	public void dispose()
    {
    }
}
