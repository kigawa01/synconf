use std::collections::HashMap;
use std::env;

use once_cell::sync::Lazy;

static COMMANDS: Lazy<HashMap<&str, Command>> = Lazy::new(|| {
    let mut commands: HashMap<&str, Command> = HashMap::new();
    commands.insert("help", Command { name: "help", func: command_help, description: "show help" });
    commands
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
    println!("__commands list__");
    for command in COMMANDS.values() {
        println!("> {}:   {}", command.name, command.description)
    }
    println!("-----------------");
    return;
}
