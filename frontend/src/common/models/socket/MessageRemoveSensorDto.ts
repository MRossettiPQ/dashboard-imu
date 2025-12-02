import { Transform } from 'class-transformer';
import { transformerDateTime } from 'src/common/models/models';
import dayjs from 'dayjs';
import { SocketEvents } from 'src/common/models/socket/SocketEvents';

export class RemoveSensorDto {
  sensor?: string;
}

export class MessageRemoveSensorDto {
  @Transform(transformerDateTime)
  date?: dayjs.Dayjs | undefined;

  type?: SocketEvents | undefined = SocketEvents.CLIENT_SERVER_REMOVE_SENSOR;

  declare content: RemoveSensorDto;

  originIdentifier?: string | undefined;
}
