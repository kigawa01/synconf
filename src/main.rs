use std::collections::HashMap;
use std::env;
use std::io::Error;

use once_cell::sync::Lazy;

use crate::install::command_install;

mod install;

static COMMANDS: Lazy<HashMap<&str, Command>> = Lazy::new(|| {
    let mut commands: HashMap<&str, Command> = HashMap::new();
    commands.insert("help", Command {
        name: "help",
        func: command_help,
        description: "show help",
    });
    commands.insert("install", Command {
        name: "install",
        func: command_install,
        description: "install synconf",
    });
    return commands;
});

struct Command {
    name: &'static str,
    func: fn(),
    description: &'static str,
}

fn main() {
    println!("hello synconf!");
    let args: Vec<String> = env::args().collect();

    if args.len() <= 1 {
        command_help();
        return;
    }


    match COMMANDS.get(args[1].as_str()) {
        None => { command_help() }
        Some(command) => { (command.func)() }
    }

    return;
}

fn command_help() {
    println!("> --commands list--");
    for command in COMMANDS.values() {
        println!("> {0: <15}: {1}", command.name, command.description)
    }
    println!("> -----------------");
    return;
}

pub fn print_error(error: Error, message: &str) -> Result<(), Error> {
    println!("Error: {}", message);
    println!("Error: {}", error);
    return Err(error);
}