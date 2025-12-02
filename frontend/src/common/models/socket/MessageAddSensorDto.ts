import { Transform } from 'class-transformer';
import { transformerDateTime } from 'src/common/models/models';
import dayjs from 'dayjs';
import { SocketEvents } from './SocketEvents';

export class AddSensorDto {
  sensor?: string;
}

export class MessageAddSensorDto {
  @Transform(transformerDateTime)
  date?: dayjs.Dayjs | undefined;

  type?: SocketEvents | undefined = SocketEvents.CLIENT_SERVER_ADD_SENSOR;

  declare content: AddSensorDto;

  originIdentifier?: string | undefined;
}
