package com.superyoudi.kdbservice.graph;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @author Sijayyx
 * @date 2026/7/16
 */
@Node
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgeTextFragmentNode {

    @Id
    private String id;
    private String text; // text其实可以不用的，因为Document与它是一一对应的关系。但是为了调试的可视化，我将它保留下来。

    @Relationship(type = "CONTAINS", direction = Relationship.Direction.OUTGOING)
    private List<KnowledgePointNode> knowledgePointNodes;

    public KnowledgeTextFragmentNode(String text) {
        this.text = text;
        id = generateHash(text); // 生成文本哈希作为唯一ID 以进行去重处理
    }

    private static String generateHash(String text) {
        // 使用 SHA-256 或 MD5
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // 使用 hashCode
            return String.valueOf(text.hashCode());
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
