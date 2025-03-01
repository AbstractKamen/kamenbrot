package com.kamenbrot.mandelbrot.io;

import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GifSequenceWriter implements AutoCloseable {

    private final IIOMetadata metadata;
    private final int delay;
    private final boolean loopContinuously;
    private final ImageWriteParam param;
    private final ImageWriter writer;
    private final ImageOutputStream os;

    public GifSequenceWriter(int delay, File outputDir, String outputName) throws IOException {
        final ImageTypeSpecifier spec = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
        this.writer = ImageIO.getImageWriters(spec, "GIF").next();
        this.os = ImageIO.createImageOutputStream(new File(outputDir, outputName + ".gif"));
        writer.setOutput(os);
        writer.prepareWriteSequence(null);
        this.param = writer.getDefaultWriteParam();
        this.metadata = writer.getDefaultImageMetadata(spec, param);
        this.delay = delay;
        this.loopContinuously = true;
        configureRootMetadata();
    }

    public void writeToSequence(BufferedImage sequenceFrame) throws IOException {
        writer.writeToSequence(new IIOImage(sequenceFrame, null, metadata), param);
    }

    // https://memorynotfound.com/generate-gif-image-java-delay-infinite-loop-example/
    private void configureRootMetadata() {
        final String metaFormatName = metadata.getNativeMetadataFormatName();
        final IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);

        final IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
        graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("delayTime", delay + "");
        graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

        // IIOMetadataNode commentsNode = getNode(root, "CommentExtensions");
        // commentsNode.setAttribute("CommentExtension", "Created by: https://memorynotfound.com");

        final IIOMetadataNode appExtensionsNode = getNode(root, "ApplicationExtensions");
        final IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
        child.setAttribute("applicationID", "NETSCAPE");
        child.setAttribute("authenticationCode", "2.0");

        final byte loop = (byte) (loopContinuously ? 0 : 1);

        child.setUserObject(new byte[]{1, loop, 0});
        appExtensionsNode.appendChild(child);

        try {
            metadata.setFromTree(metaFormatName, root);
        } catch (IIOInvalidTreeException e) {
            throw new RuntimeException(e);
        }
    }

    private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        for (int i = 0; i < rootNode.getLength(); i++) {
            if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                return (IIOMetadataNode) rootNode.item(i);
            }
        }
        final IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return (node);
    }

    @Override
    public void close() throws IOException {
        writer.endWriteSequence();
        os.close();
    }
}
