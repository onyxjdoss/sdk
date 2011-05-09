package org.gitana.sdk.loader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.gitana.repo.association.Direction;
import org.gitana.repo.client.Branch;
import org.gitana.repo.client.Repository;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.gitana.repo.client.SecurityUser;
import org.gitana.repo.client.nodes.Association;
import org.gitana.repo.client.nodes.Node;
import org.gitana.repo.client.services.Branches;
import org.gitana.repo.client.services.Definitions;
import org.gitana.repo.client.services.Nodes;
import org.gitana.repo.namespace.QName;
import org.gitana.repo.query.QueryBuilder;
import org.gitana.util.ClasspathUtil;
import org.gitana.util.JsonUtil;

/**
 * Loader for setting up users.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: drq
 * Date: 4/20/11
 * Time: 9:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class SocialGraphLoader extends AbstractLoader {

    private Map<String, Repository> gitanaRepositories;
    private String socialGraphLoadMode;
    private ObjectNode socialGraphObj;
    private Map<String, SecurityUser> gitanaUsers;
    private Repository repository;
    private Branch branch;

    private static Log logger = LogFactory.getLog(SocialGraphLoader.class);

    /**
     * @throws Exception
     */
    public SocialGraphLoader(Repository repository) throws Exception {
        super();
        this.repository = repository;
        this.branch = repository.branches().read("master");
        this.gitanaUsers = gitana.users().map();
        this.socialGraphLoadMode = this.loaderConfig.getString("socialGraph.load.mode") == null ? "update" : this.loaderConfig.getString("socialGraph.load.mode");
        this.socialGraphObj = this.loadJsonFromClasspath("org/gitana/sdk/loader/social/social-graph.json");
    }

    public Node getSecurityPrincipalNode(JsonNode nodeObj) {
        Node node = null;
        if (nodeObj.get("type").getTextValue().equals("user")) {
            node = branch.nodes().readPerson(nodeObj.get("id").getTextValue(), true);
        }
        if (nodeObj.get("type").getTextValue().equals("group")) {
            node = branch.nodes().readGroup(nodeObj.get("id").getTextValue(), true);
        }
        return node;
    }

    /**
     *
     */
    public void loadSocialGraph() throws Exception {
        JsonNode usersObj = socialGraphObj.get("users");
        if (usersObj != null) {
            Iterator<String> it = usersObj.getFieldNames();
            while (it.hasNext()) {
                String userId = it.next();
                this.loadUser(userId, usersObj.get(userId));
            }
        }

        // Manage association types
        if (socialGraphObj.get("associationTypes") != null) {
            for (JsonNode associationTypeObj : socialGraphObj.get("associationTypes")) {
                String qnameStr = associationTypeObj.getTextValue();
                QName qname = QName.create(qnameStr);
                branch.definitions().defineAssociationType(qname);
                logger.info("Create association type  :: " + qnameStr);
            }

        }
        // Manage associations
        if (socialGraphObj.get("associations") != null) {
            for (JsonNode associationObj : socialGraphObj.get("associations")) {
                String typeQnameStr = associationObj.get("associationType").getTextValue();
                QName typeQname = QName.create(typeQnameStr);
                Node sourceNode = this.getSecurityPrincipalNode(associationObj.get("source"));
                Node targetNode = this.getSecurityPrincipalNode(associationObj.get("target"));
                Direction direction = Direction.BOTH;
                if (associationObj.get("direction") != null) {
                    direction = Direction.valueOf(associationObj.get("direction").getTextValue());
                }
                if (sourceNode != null && targetNode != null) {
                    Association association = sourceNode.associate(targetNode, typeQname,direction);
                    logger.info("Create " + typeQnameStr + " Association :: " + sourceNode.getQName() + "<=>" + targetNode.getQName() + " (" + direction + ")");
                    logger.info("Updated Association ::" + association.toJSONString(true));
                    JsonNode associationDetailsObj = associationObj.get("associationDetails");

                    if (associationDetailsObj != null) {
                        /*
                        Iterator<String> it = associationDetailsObj.getFieldNames();
                        while (it.hasNext()) {
                            String fieldName = it.next();
                            association.set(fieldName, associationDetailsObj.get(fieldName));
                        }
                        */
                        association.set("details", associationDetailsObj);
                        association.update();
                        logger.info("Updated Association ::" + association.toJSONString(true));
                    }
                }
            }
        }
    }

    /**
     * @param userId
     * @param userObj
     * @throws Exception
     */
    public void loadUser(String userId, JsonNode userObj) throws Exception {
        logger.info("Loading Person Object for user ::" + userId);
        Node personNode = branch.nodes().readPerson(userId, true);
        if (personNode != null) {
            logger.info("Get Person Node for user ::" + userId);
            logger.info("Person Node ::" + personNode.toJSONString(true));
            Iterator<String> it = userObj.getFieldNames();
            while (it.hasNext()) {
                String fieldName = it.next();
                personNode.set(fieldName, userObj.get(fieldName));
            }
            personNode.update();
            logger.info("Updated Person Node ::" + personNode.toJSONString(true));
        }
    }

}
