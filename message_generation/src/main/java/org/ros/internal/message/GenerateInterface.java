/*
 * Copyright (C) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.internal.message;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import org.ros.exception.RosMessageRuntimeException;
import org.ros.internal.message.definition.MessageDefinitionReflectionProvider;
import org.ros.internal.message.definition.MessageDefinitionTupleParser;
import org.ros.message.MessageDeclaration;
import org.ros.message.MessageFactory;
import org.ros.message.MessageIdentifier;

import com.google.common.collect.Lists;

/**
 * @author d.stonier@gmail.com (Daniel Stonier)
 */
public class GenerateInterface {

  private static void writeInterface(MessageDeclaration messageDeclaration, File outputDirectory,
      boolean addConstantsAndMethods, MessageFactory messageFactory) {
    MessageInterfaceBuilder builder = new MessageInterfaceBuilder();
    builder.setPackageName(messageDeclaration.getPackage());
    builder.setInterfaceName(messageDeclaration.getName());
    builder.setMessageDeclaration(messageDeclaration);
    builder.setAddConstantsAndMethods(addConstantsAndMethods);
    try {
      String content;
      content = builder.build(messageFactory);
      File file = new File(outputDirectory, messageDeclaration.getType() + ".java");
      System.out.println("Output File: " + file.getAbsolutePath());
      FileUtils.writeStringToFile(file, content);
    } catch (Exception e) {
      System.out.printf("Failed to generate interface for %s.\n", messageDeclaration.getType());
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    List<String> arguments = Lists.newArrayList(args);
    if (arguments.size() != 3) {
      System.out
          .println("Incorrect usage, please provide two args: _output_directory_, _pkg_ and _path_to_msg/srv_file_");
      System.exit(1);
    }
    File outputDirectory = new File(arguments.remove(0));
    String pkg = arguments.remove(0);
    File file = new File(arguments.remove(0));

    System.out.println("Output Directory: " + outputDirectory.getAbsolutePath());
    System.out.println("Package: " + pkg);
    System.out.println("Message: " + file.getAbsolutePath());

    String name = FilenameUtils.getBaseName(file.getName());
    String extension = FilenameUtils.getExtension(file.getName());

    System.out.println("  Name: " + name);
    System.out.println("  Extension: " + extension);
    String definition;
    try {
      definition = FileUtils.readFileToString(file, "US-ASCII");
    } catch (IOException e) {
      throw new RosMessageRuntimeException(e);
    }
    MessageIdentifier messageIdentifier = MessageIdentifier.of(pkg, name);
    MessageDeclaration messageDeclaration = new MessageDeclaration(messageIdentifier, definition);
    MessageDefinitionReflectionProvider messageDefinitionProvider =
        new MessageDefinitionReflectionProvider();
    messageDefinitionProvider.add(messageIdentifier.getType(), definition);
    MessageFactory messageFactory = new DefaultMessageFactory(messageDefinitionProvider);
    if (extension.equals("msg")) {
      writeInterface(messageDeclaration, outputDirectory, true, messageFactory);
    } else if (extension.equals("srv")) {
      writeInterface(messageDeclaration, outputDirectory, false, messageFactory);
      List<String> requestAndResponse = MessageDefinitionTupleParser.parse(definition, 2);
      MessageDeclaration requestDeclaration =
          MessageDeclaration.of(messageIdentifier.getType() + "Request", requestAndResponse.get(0));
      MessageDeclaration responseDeclaration =
          MessageDeclaration
              .of(messageIdentifier.getType() + "Response", requestAndResponse.get(1));
      writeInterface(requestDeclaration, outputDirectory, true, messageFactory);
      writeInterface(responseDeclaration, outputDirectory, true, messageFactory);
    }
  }
}
