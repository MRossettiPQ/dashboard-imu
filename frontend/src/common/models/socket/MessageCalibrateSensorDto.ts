import { Transform } from 'class-transformer';
import { transformerDateTime } from 'src/common/models/models';
import dayjs from 'dayjs';
import { SocketEvents } from 'src/common/models/socket/SocketEvents';

export class CalibrateSensorDto {
  sensor?: string;
}

export class MessageCalibrateSensorDto {
  @Transform(transformerDateTime)
  date?: dayjs.Dayjs | undefined;

  type?: SocketEvents | undefined = SocketEvents.CLIENT_SERVER_CALIBRATE;

  declare content: CalibrateSensorDto;

  originIdentifier?: string | undefined;
}
