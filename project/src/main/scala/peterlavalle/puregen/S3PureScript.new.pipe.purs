foreign import _<@name/> ::
  -- basic "null" constructor
    (Unit -> (Maybe <@name/>Event)) ->
  -- event constructors
    (<@events/>) ->
  -- struct constructors
    (<@structs/>) ->
  -- args
    <@args/> ->
  -- the resulting handle thing
    Effect <@name/>
