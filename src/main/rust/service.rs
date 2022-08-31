use std::io::{BufWriter, Write};
use std::net::{SocketAddr, TcpStream, ToSocketAddrs};
use std::process::{Command, exit};

use crate::errors::PrintErr;

pub fn start() {
    let command = Command::new("java").arg("-jar").arg("synconf.jar")
        .current_dir("/var/synconf");
    match command.spawn() {
        Ok(_) => {}
        Err(e) => {
            PrintErr::from_message_error("could not start synconf", Box::new(e))
        }
    }
}

pub fn stop() {
    let host = "localhost:10000";
    let mut addresses = match host.to_socket_addrs() {
        Ok(addresses) => { addresses }
        Err(e) => {
            PrintErr::from_message_error("could not create address", Box::from(e));
            return;
        }
    };
    let address = match addresses.find(|address| address.is_ipv4()) {
        None => {
            PrintErr::from_message("could not find address")
            return;
        }
        Some(address) => { address }
    }

    let stream = match TcpStream::connect(address) {
        Ok(stream) => { stream }
        Err(e) => {
            PrintErr::from_message_error("could not connect to address", Box::from(e));
            return;
        }
    };
    let mut writer = BufWriter::new(stream);
    match writer.write_all(b"end\n") {
        Ok(_) => {}
        Err(e) => {
            PrintErr::from_message_error("could not send end",Box::from(e));
            return;
        }
    }
    match writer.flush() {
        Ok(_) => {}
        Err(e) => {
            PrintErr::from_message_error("could not flash stream",Box::from(e));
            return;
        }
    }

}