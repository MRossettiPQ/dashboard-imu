export class RegisterRequestDto {
  username!: string;
  name!: string;
  email!: string;
  password?: string;
  passwordConfirm?: string;
}
