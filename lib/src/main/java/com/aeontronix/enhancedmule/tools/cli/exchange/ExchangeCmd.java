/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli.exchange;

import com.aeontronix.enhancedmule.tools.cli.AbstractCommand;
import picocli.CommandLine.Command;

@Command(name = "exchange", aliases = "ex", subcommands = {ExchangePromoteApplicationCmd.class, ExchangePublishAssetCmd.class})
public class ExchangeCmd extends AbstractCommand {
}
