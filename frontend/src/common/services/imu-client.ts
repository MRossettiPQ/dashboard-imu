export class Patient {
  retrieve() {
    console.log('normal');
  }
}

export class User {
  retrieve() {
    console.log('normal');
  }
}

export class Session {
  retrieve() {
    console.log('normal');
  }
}

export class Auth {
  retrieve() {
    console.log('normal');
  }
}

export class ImuClient {
  auth = new Auth()
  user = new User()
  patient = new Patient()
  session = new Session()

  normal() {
    console.log('normal');
  }
}
