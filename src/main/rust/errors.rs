use std::error;
use std::fmt;
use std::fmt::Debug;

pub struct Error {
    err: Errors,
}

pub struct PrintErr;

impl PrintErr {
    pub fn from_messages_errors<T>(messages: Vec<String>, errors: Vec<Box<dyn error::Error>>) -> Result<T, Error>
    {
        for i in 0..messages.len() {
            print_error(messages[i].as_str());
        }
        for i in 0..errors.len() {
            print_error(errors[i].to_string());
        }
        Err(Error::from_messages_errors(messages, errors))
    }
    pub fn from_message_error<T>(message: &str, error: Box<dyn error::Error>) -> Result<T, Error>
    {
        let mut messages = Vec::new();
        let mut errors = Vec::new();
        messages.push(message.to_string());
        errors.push(error);
        return PrintErr::from_messages_errors(messages, errors);
    }
    pub fn from_message<T>(message: &str) -> Result<T, Error> {
        let mut messages = Vec::new();
        let errors = Vec::new();
        messages.push(message.to_string());
        return PrintErr::from_messages_errors(messages, errors);
    }
    pub fn from_error<T>(err: Box<dyn error::Error>) -> Result<T, Error> {
        let messages = Vec::new();
        let mut errors = Vec::new();
        errors.push(err);
        return PrintErr::from_messages_errors(messages, errors);
    }
}

impl Error {
    pub fn from_messages_errors(messages: Vec<String>, errors: Vec<Box<dyn error::Error>>) -> Error {
        Error {
            err: Errors { messages, errors }
        }
    }
}

struct Errors {
    messages: Vec<String>,
    errors: Vec<Box<dyn error::Error>>,
}

impl fmt::Display for Error {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        for i in 0..self.err.messages.len() {
            let result = f.write_str(self.err.messages[i].as_str());
            if let Err(_) = result { return result; }
        }
        for i in 0..self.err.errors.len() {
            let result = Debug::fmt(&self.err.errors[i], f);
            if let Err(_) = result { return result; }
        }
        return Ok(());
    }
}

impl error::Error for Error {
    fn source(&self) -> Option<&(dyn error::Error + 'static)> {
        for i in 0..self.err.errors.len() {
            let result = self.err.errors[i].source();
            if let Some(_) = result { return result; }
        }
        return None;
    }
}


impl Debug for Error {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        <Self as fmt::Display>::fmt(self, f)
    }
}

fn print_error<T>(err: T)
    where T: fmt::Display
{
    println!("Error: {}", err)
}