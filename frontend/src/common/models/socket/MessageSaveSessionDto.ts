import { Transform } from 'class-transformer';
import { transformerDateTime } from 'src/common/models/models';
import dayjs from 'dayjs';
import { SocketEvents } from 'src/common/models/socket/SocketEvents';

export class SaveSessionDto {
  sensor?: string;
}

export class MessageSaveSessionDto {
  @Transform(transformerDateTime)
  date?: dayjs.Dayjs | undefined;

  type?: SocketEvents | undefined;

  declare content: SaveSessionDto;

  originIdentifier?: string | undefined;
}
