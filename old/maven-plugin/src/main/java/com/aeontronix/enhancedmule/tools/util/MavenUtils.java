/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.util;

import com.aeontronix.commons.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.repository.AuthenticationSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MavenUtils {
    private static final Logger logger = LoggerFactory.getLogger(MavenUtils.class);

    public static File getProjectJar(MavenProject project) throws MojoExecutionException {
        if (logger.isDebugEnabled()) {
            logger.debug("Listing attached artifacts : " + project.getAttachedArtifacts());
        }
        for (Object artifactObj : project.getAttachedArtifacts()) {
            Artifact artifact = (Artifact) artifactObj;
            if (logger.isDebugEnabled()) {
                logger.debug("Found : " + artifact.getFile() + " of classifier " + artifact.getClassifier());
            }
            if (artifact.getClassifier() != null && artifact.getClassifier().equals("mule-application")) {
                logger.debug("File is mule-application");
                return artifact.getFile();
            } else if (logger.isDebugEnabled()) {
                logger.debug("File is not mule-application");
            }
        }
        for (Object artifactObj : project.getAttachedArtifacts()) {
            Artifact artifact = (Artifact) artifactObj;
            if (artifact.getType().equals("zip") && StringUtils.isBlank(artifact.getClassifier())) {
                return artifact.getFile();
            }
        }
        throw new MojoExecutionException("No mule-application attached artifact found");
    }

    public static boolean isTemplateOrExample(MavenProject project) {
        if (project != null) {
            for (Object artifactObj : project.getAttachedArtifacts()) {
                Artifact artifact = (Artifact) artifactObj;
                String classifier = artifact.getClassifier();
                if (classifier != null && (classifier.equals("mule-application-template") || classifier.equals("mule-application-example"))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void addRepositoryUsernamePassword(AuthenticationSelector authenticationSelector, String id, String username, SecretResolver secretResolver) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<? extends AuthenticationSelector> selectorClass = authenticationSelector.getClass();
        if (!selectorClass.getName().equals("org.eclipse.aether.util.repository.DefaultAuthenticationSelector")) {
            throw new IllegalStateException("Unsupported maven authentication selector:" + selectorClass.getName());
        }
        ClassLoader cl = selectorClass.getClassLoader();
        Class<?> authClass = cl.loadClass("org.eclipse.aether.repository.Authentication");

        final Object authProxy = Proxy.newProxyInstance(cl, new Class<?>[]{authClass}, new InvocationHandler() {
            public String secret;
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                final String methodName = method.getName();
                switch (methodName) {
                    case "hashCode":
                        return this.hashCode();
                    case "digest":
                        loadSecret();
                        args[0].getClass().getMethod("update",String[].class).invoke(args[0],new Object[] { new String[] {"password",secret} });
                        return null;
                    case "fill":
                        loadSecret();
                        args[0].getClass().getMethod("put", String.class,Object.class).invoke(args[0],"password",secret);
                        return null;
                    default:
                        return null;
                }
            }

            private void loadSecret() throws Exception {
                if( secret == null ) {
                    secret = secretResolver.getSecret();
                }
            }
        });
//        Class<?> pAuthCl = cl.loadClass("org.eclipse.aether.util.repository.SecretAuthentication");
//        final Constructor<?> sAuthClConstructor = pAuthCl.getDeclaredConstructor(String.class, String.class);
//        sAuthClConstructor.setAccessible(true);
//        final Object passwordAuthObject = sAuthClConstructor.newInstance("password", password);

        Class<?> authBuilderClass = cl.loadClass("org.eclipse.aether.util.repository.AuthenticationBuilder");
        Object authBuilder = authBuilderClass.getConstructor().newInstance();
        authBuilderClass.getMethod("addUsername", String.class).invoke(authBuilder, username);
        authBuilderClass.getMethod("addCustom", authClass).invoke(authBuilder, authProxy);
        Object authObj = authBuilderClass.getMethod("build").invoke(authBuilder);
        selectorClass.getMethod("add", String.class, authClass).invoke(authenticationSelector, id, authObj);
    }

    public interface SecretResolver {
        String getSecret() throws Exception;
    }
}
